require 'logger'

class GraphA
  def initialize
    @graph = {
      a: [:b, :c, :e],
      b: [:d, :f, :h],
      c: [:g],
      d: [],
      e: [],
      f: [:e],
      g: [],
      h: []
    }
  end

  def edges(vertex)
    @graph[vertex]
  end
end

class WolfGoatCabbageGraph
  class Vertex
    def initialize(state)
      @state = state
      @logger = Logger.new(STDOUT)
      @logger.level = Logger::WARN
    end
    
    def ==(other)
      left_bank = (@state[0] - other.left_bank()).empty?
      right_bank = (@state[2] - other.right_bank()).empty?
      result = left_bank && right_bank
      @logger.debug("comparing #{self} to #{other}, our left bank #{@state[0]}, their left bank #{other.left_bank()}, our right bank #{@state[2]}, their right bank #{other.right_bank()} result #{result}")
      result
    end
    
    def eql?(other)
      @logger.debug("somebody's checking eql?")
      other.class == Vertex && self == other
    end
    
    def to_s()
      "[#{@state[0].join(" ")}] [#{@state[1].join(" ")}] [#{@state[2].join(" ")}]"
    end
    
    def left_bank()
      @state[0]
    end
    
    def right_bank()
      @state[2]
    end
  end
  
  def initialize()
    @logger = Logger.new(STDOUT)
    @logger.level = Logger::WARN
  end
  
  def generate_wgb_moves(start)
    @logger.debug("generating moves for #{start}")
    
    left_bank = start.left_bank()
    right_bank = start.right_bank()
    source_bank = left_bank.include?(:farmer) ? left_bank : right_bank
    target_bank = left_bank.include?(:farmer) ? right_bank : left_bank
    source_bank_on_left = left_bank.include?(:farmer)

    target_banks = generate_target_banks(source_bank, target_bank, source_bank_on_left)
    target_banks.each do |target_bank|
      @logger.debug("generated #{target_bank}")
    end
    target_banks
  end

  def valid_bank?(bank)
    return false if bank.include?(:wolf) && bank.include?(:goat) && !bank.include?(:farmer)
    return false if bank.include?(:goat) && bank.include?(:cabbage) && !bank.include?(:farmer)
    return true
  end

  def generate_target_banks(source_bank, current_target_bank, source_goes_on_left)
    source_bank_no_farmer = source_bank.select{|member| member != :farmer}
    potential_target_banks = source_bank_no_farmer.inject([]) do |memo, val| 
      potential_bank = (current_target_bank + [:farmer, val])
      if source_goes_on_left
        memo << [source_bank_no_farmer.reject{|member| val == member}, [:boat], potential_bank]
      else
        memo << [potential_bank, [:boat], source_bank_no_farmer.reject{|member| val == member}]
      end
      memo
    end
    if source_goes_on_left # handle case where farmer doesn't take anything
      potential_target_banks << [source_bank_no_farmer, [:boat], current_target_bank + [:farmer]]
    else
      potential_target_banks << [current_target_bank + [:farmer], [:boat], source_bank_no_farmer]
    end
    potential_target_banks.select {|banks| valid_bank?(banks[0]) && valid_bank?(banks[2])}.collect{|banks| Vertex.new(banks)}
  end
    
  def edges(vertex)
    generate_wgb_moves(vertex)
  end
  
  def build_vertex(state)
    Vertex.new(state)
  end
end


def search(graph, start, stop)
  logger = Logger.new(STDOUT)
  logger.level = Logger::WARN
  
  def search_recur(graph, vertex, stop, visited, trail, logger)    
    logger.debug("visiting #{vertex.to_s()}")
    
    if vertex == stop
      return trail + [vertex]
    end
    
    for adjacent_vertex in graph.edges(vertex)
      # make move
      logger.debug("does visited include #{adjacent_vertex}? #{visited.include?(adjacent_vertex)}")
      if !visited.include? (adjacent_vertex)
        logger.debug("adding #{adjacent_vertex} to visited")
        visited << adjacent_vertex
                
        potential_trail = search_recur(graph, adjacent_vertex, stop, visited, trail + [vertex], logger)
        
        if potential_trail[potential_trail.length - 1] != vertex # we added vertexes, so we must have found something; let's short curcuit and return the trail
          return potential_trail
        end
      end
    end
    trail
  end
  
  visited = []
  visited << start
  return search_recur(graph, start, stop, visited, [], logger)
end

#trail = search(GraphA.new, :a, :e)
#if trail.empty?
#  puts "did not find stop point"
#else
#  puts trail.join(" ")
#end

graph = WolfGoatCabbageGraph.new
trail = search(graph, 
  graph.build_vertex([[:wolf, :goat, :cabbage, :farmer],[:boat],[]]), 
  graph.build_vertex([[],[:boat],[:wolf,:goat,:cabbage,:farmer]]))
  
if trail.nil?
  puts "couldn't find a successful result"
else
  puts "result:"
  trail.each do |trail_move|
    puts trail_move.to_s()
  end
end