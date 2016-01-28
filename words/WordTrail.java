import java.io.*;
import java.util.*;

public class WordTrail {
	public static int characterDifference(String wordA, String wordB) {
		if (wordA.length() != wordB.length()) {
			throw new RuntimeException("characterDiffernence can only compare strings of the same length");
		}
		
		int count = 0;
		for (int i = 0; i < wordA.length(); i++) {
			if (wordA.charAt(i) != wordB.charAt(i)) {
				count += 1;
			}
		}
		return count;
	}
	
	// http://codereview.stackexchange.com/questions/32354/implementation-of-dijkstras-algorithm
	
	static class WordDistance {
		private String word;
		private int distance;
		
		public WordDistance(String word, int distance) {
			this.word = word;
			this.distance = distance;
		}
		
		public String getWord() {
			return this.word;
		}
		
		public int distanceFrom(String otherWord) {
			return WordTrail.characterDifference(word, otherWord);
		}
	}
	
	static class Dictionary {
		private int wordSize;
		private List<WordDistance> dictionary;
		
		public Dictionary(String endWord) {
			this.wordSize = endWord.length();
			this.dictionary = new ArrayList<WordDistance>();
			initialize(endWord);
		}
		
		private void initialize(String endWord) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./dictionary.txt"))));
				String unstrippedWord;
				while ((unstrippedWord = reader.readLine()) != null) {
					final String word = unstrippedWord.trim();
					if (word.length() == wordSize) {
						dictionary.add(new WordDistance(word, WordTrail.characterDifference(word, endWord)));
					}
				}
				reader.close();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		
		public List<WordDistance> getWordsOneCharacterAway(String word) {
			List<WordDistance> oneAwayWords = new ArrayList<WordDistance>();
			for (int i = 0; i < dictionary.size(); i++) {
				if (dictionary.get(i).distanceFrom(word) == 1) {
					oneAwayWords.add(dictionary.get(i));
				}
			}
			return oneAwayWords;
		}
		
		public int distanceForWord(String word) {
			for (int i = 0; i < dictionary.size(); i++) {
				if (dictionary.get(i).word.equals(word)) {
					return dictionary.get(i).distance;
				}
			}
			throw new RuntimeException("Could not find distance for word "+word);
		}
		
		public List<String> allWords() {
			List<String> words = new ArrayList<String>();
			for (WordDistance wordDistance : dictionary) {
				words.add(wordDistance.getWord());
			}
			return words;
		}
	}
	
	static class WordGraph {
		private Dictionary dictionary;
		
		public WordGraph(Dictionary dictionary) {
			this.dictionary = dictionary;
		}
		
		public List<String> neighbors(String word) {
			List<String> neighborsArray = new ArrayList<String>();
			for (WordDistance wordDistance : dictionary.getWordsOneCharacterAway(word)) {
				neighborsArray.add(wordDistance.getWord());
			}
			return neighborsArray;
		}
		
		public int weight(String word) {
			return dictionary.distanceForWord(word);
		}
		
		public List<String> allValues() {
			return dictionary.allWords();
		}
	}
	
	static class ShortestPathFinder {
		private WordGraph graph;
		private String startPoint;
		private String endPoint;
		private Map<String, Integer> distances = new HashMap<String, Integer>();
		private List<String> trail = new ArrayList<String>();
		
		private ShortestPathFinder(WordGraph graph, String startPoint, String endPoint) {
			this.graph = graph;
			this.startPoint = startPoint;
			this.endPoint = endPoint;
			for (String word : graph.allValues()) {
				if (word.equals(startPoint)) {
					distances.put(word, new Integer(0));
				} else {
					distances.put(word, Integer.MAX_VALUE);
				}
			}
		}
		
		private List<String> findShortest() {
			System.out.println("finding shortest");
			final Queue<WeightedNode> queue = new PriorityQueue<WeightedNode>(100, WeightedNode.getComparator());
			List<String> trail = new ArrayList<String>();
			trail.add(this.startPoint);
			
			for (String word : graph.neighbors(this.startPoint)) {
				queue.add(new WeightedNode(word, WordTrail.characterDifference(word, this.endPoint)));
			}
			
			final Set<String> doneSet = new HashSet<String>();
			
			while (!queue.isEmpty()) {
				WeightedNode curr = queue.poll();
				trail.add(curr.getValue()); // we need to be more selective here...we'll figure out how to do that
				
				if (curr.getValue().equals(this.endPoint)) {
					return trail;
				}
				
				doneSet.add(curr.getValue());
				
				for (String adjacentToNeighbor : graph.neighbors(curr.getValue())) {
					if (!doneSet.contains(adjacentToNeighbor)) {
						int adjacentWeight = distances.get(adjacentToNeighbor);
						int newWeight = curr.getDistance() + WordTrail.characterDifference(curr.getValue(), adjacentToNeighbor);
						if (newWeight < adjacentWeight) {
							distances.put(adjacentToNeighbor, newWeight);
							queue.add(new WeightedNode(adjacentToNeighbor, WordTrail.characterDifference(adjacentToNeighbor, this.endPoint)));
						}
					}
				}
			}
			
			return null;
		}
		
		public static List<String> findShortestPath(WordGraph graph, String startPoint, String endPoint) {
			ShortestPathFinder shortestPathFinder = new ShortestPathFinder(graph, startPoint, endPoint);
			return shortestPathFinder.findShortest();
		}
		
		private static class WeightedNode {
			private String value;
			private int distance;
			
			WeightedNode(String value, int distance) {
				this.value = value;
				this.distance = distance;
			}
			
			public String getValue() {
				return this.value;
			}
			
			public int getDistance() {
				return this.distance;
			}
			
			@Override
			public boolean equals(Object other) {
				if (other instanceof WeightedNode) {
					WeightedNode otherNode = (WeightedNode)other;
					return value.equals(otherNode.value);
				}
				return false;
			}
			
			@Override
			public int hashCode() {
				int hash = 1;
				hash = hash * 19 + value.hashCode();
				return hash;
			}
			
			public static Comparator<WeightedNode> getComparator() {
				return new Comparator<WeightedNode>() {
					public int compare(WeightedNode a, WeightedNode b) {
						return a.distance - b.distance;
					}

					public boolean equals(Object other) {
						return other.getClass().equals(this.getClass());
					}
				};
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Proper usage: java WorldTrail {begin word} {end word}");
			return;
		}
		
		final String beginWord = args[0].toLowerCase();
		final String endWord = args[1].toLowerCase();
		
		if (beginWord.length() != endWord.length()) {
			System.err.println("The begin and end word must be of the same length");
			return;
		}
		
		System.out.println("begin: "+beginWord+"; end: "+endWord);
		
		Dictionary dictionary = new Dictionary(endWord);
		WordGraph wordGraph = new WordGraph(dictionary);
		
		List<String> trail = ShortestPathFinder.findShortestPath(wordGraph, beginWord, endWord);
		
		if (trail == null) {
			System.out.println("Could not find path between "+beginWord+" and "+endWord);
		} else {
			for (String wordInTrail : trail) {
				System.out.println(wordInTrail);
			}
		}
	}
}