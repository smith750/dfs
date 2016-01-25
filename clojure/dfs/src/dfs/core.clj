(ns dfs.core
  (:gen-class))

(def edges [vertex]
	(let [graph {
		:a [:b :c :e]
		:b [:d :f :h]
		:c [:g]
		:f [:e]
	}]
		(get-in graph vertex [])))
		
(def search[graph search stop]
	(letfn [(search-recur [graph vertex stop visited trail]
		(if (= vertex stop) trail
		(comment
			for adjacent_vertex in graph.edges(vertex)
	      # make move
	      if !visited.include? (adjacent_vertex)
	        potential_trail = search_recur(graph, adjacent_vertex, stop, visited, trail + [vertex])
	        if potential_trail[potential_trail.length - 1] != vertex # we added vertexes, so we must have found something; let's short curcuit and return the trail
	          return potential_trail
	        end
	      end
	    end
	    trail
			)
		
		))]
		(search-recur graph start stop #{} [])))

(defn -main
  [& args]
  (let [
		trail (search graph :a :e)
		msg (if (empty? trail) "did not find stop point" (str (join trail " ")))
		]
	(do
		(println "Hello, World!")
		(println msg)
		)))
