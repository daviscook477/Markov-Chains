package greet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Chain
{
	
	// Data structure for storing a word and the number of times it has appeared in the text.
	private class WordCount
	{
		
		String word; // The word.
		int count; // The number of times it has appeared.
		
		public WordCount(String w)
		{
			word = w;
			count = 1;
		}
		
		// Increment number of times appeared by 1
		public void increment()
		{
			count++;
		}
		
	}
	
	// Data structure for storing a set of word keys like ['he', 'is'] with its respective word counts.
	// This pairs each key with a list of WordCounts where each word count is a word and the number of times
	// it has appeared after the key in the text.
	private class Pair
	{
		
		public String[] keys;
		public ArrayList<WordCount> wordCounts;
		
		// Pairs are made with the key (String[] k) and a string that is the first word that followed
		// the key in the text.
		public Pair(String[] k, String str)
		{
			keys = k;
			wordCounts = new ArrayList<WordCount>();
			WordCount wc = new WordCount(str);
			wordCounts.add(wc);
		}
		
		
	}
	
	// CONFIG
	
	public static final int COULD_NOT_READ_INPUT_ERROR = 1;
	public static final int FILE_ALREADY_EXISTS_ERROR = 2;
	public static final int COULD_NOT_WRITE_OUTPUT_ERROR = 3;
	
	public static final String INPUT_FILE_PATH = "examples\\corups_20000_leagues_under_the_sea.txt";
	public static final String OUTPUT_FILE_PATH = "examples\\output_20000_leagues_under_the_sea.txt";
	
	public static final int DEFAULT_WORDS_TO_GENERATE = 1000;
	
	// END CONFIG
	
	public static void main(String[] args) throws IOException
	{
		File f = new File(INPUT_FILE_PATH);
		FileReader fr = null;
		String text = "";
		try {
			fr = new FileReader(f);
			text = "";
			int a;
			int numChars = 0;
			int maxChars = 200000;
			while ((a = fr.read()) != -1 && numChars <= maxChars)
			{
				String s = ""+(char) a;
				text += s;
				numChars++;
			}
		} catch (IOException e) {
			System.err.println("Could not read file at path: " + INPUT_FILE_PATH);
			System.exit(COULD_NOT_READ_INPUT_ERROR);
		} finally {
			if (fr != null) {
				fr.close();
			}
		}
		// check if the file exists BEFORE making the Chain (b/c otherwise the CPU time making the chain could be wasted)
		File fileToWriteTo = new File(OUTPUT_FILE_PATH);
		if (fileToWriteTo.exists()) { // don't write to existing files
			System.err.println("File already exists at path: " + OUTPUT_FILE_PATH + " Chain will not overwrite existing files.");
			System.exit(FILE_ALREADY_EXISTS_ERROR);
		}
		Chain c = new Chain(2, text); // build Markov Chain off of the corpus
		String[] texta = c.genText(DEFAULT_WORDS_TO_GENERATE); // generate some number of words using the Markov Chain
		String full = "";
		for ( int i = 0; i < texta.length; i++)
		{
			full+=texta[i]+" ";
		}
		// replace quotes because they don't work out right (the chain has no idea that something started with quotes so it often forgets to end them)
		full = full.replace("\"", "");
		String[] splits = full.split("[\\!|\\?|\\.]"); // split into sentences (b/c the chain doesn't have any idea about paragraphs -> this makes it format nicer)
		FileWriter fw = null;
		try { // write to file
			fw = new FileWriter(OUTPUT_FILE_PATH); // write to the output file path
			for (int i = 1; i < splits.length - 1; i++) // start at one to skip the sentence fragment (and the last one is just a '.' so skip it too)
			{
				fw.write(splits[i].substring(1)); // substring removes preceeding space (' ')
				fw.write(".\n");
			}
		} catch (IOException e) {
			System.err.println("Could not write to file at path: " + OUTPUT_FILE_PATH);
			System.exit(COULD_NOT_WRITE_OUTPUT_ERROR);
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
	
	private int o; // Order of the Chain i.e. number of words per key.
	private ArrayList<Pair> p; // The pairs of keys and word distributions.

	// Constructs a Markov Chain of order from text.
	public Chain(int order, String text)
	{
		// Separate the human text into tokens for markov chain generation.
		// The text becomes an array containing each word.
		// It is split on spaces. " "
		String[] elems = splitInput(text);
		
		// The Markov Chain's pairs of keys and values. Built with a starting capacity that is the maximum number of
		// key values pairs that could exist.
		// Note that large of a starting capacity is excessive.
		ArrayList<Pair> pairs = new ArrayList<Pair>(maxNumPairs(order, elems.length));
		
		// Build a Markov Chain from the input.
		// Iterate over each element in the list and generate the pair for it word keys and the word that followed the key.
		// Whenever a word key has already appeared in the text, the associated probability distribution is updated instead of
		// having a new pair created.
		for (int i = 0; i < elems.length; i++)
		{
			// Confirm that there still exists enough elements in the list to create another key, value pair.
			if (hasNextKey(order, i, elems.length))
			{
				// Look at the key (first order elements in the array from the current index).
				String[] key = getKey(order, i, elems);
				// Find the index for the key in the array list of keys-value pairs.
				int keyIndex = indexKeyExists(pairs, key);
				if (keyIndex == -1) // If the key doesn't exist, it must be generated.
				{
					// Add a key-value pair created from the current index.
					pairs.add(createPair(elems, order, i));
				}
				else // The key does exist.
				{
					// Because the key exists, now the word that comes after the key this time must be added.
					
					// Get the probability distribution for the key.
					ArrayList<WordCount> wcs = pairs.get(keyIndex).wordCounts;
					
					// Next check to see if the word has already come after this key before.
					// If it does, the number of times the word has been seen is incremented by one.
					// If it doesn't then add a new word to the probability distribution with count=1
					boolean exists = false;
					String currentString = elems[i + order];
					// Look at all of the word counts to see if this word has already come after this word.
					for (int j = 0; j < wcs.size(); j++)
					{
						WordCount wc = wcs.get(j);
						// Check if the word does exist already.
						if (currentString.equals(wc.word))
						{
							// It does exist so:
							// increment the number of times it has been seen by one.
							wc.increment();
							exists = true;
							// Don't bother looking anymore. Doesn't make sense.
							break;
						}
					}
					// If the word hasn't been found before
					if (!exists)
					{
						// Add the new word to the prob. distri.
						wcs.add(new WordCount(currentString));
					}
				}
				
			}
			else
			{
				// There no longer exist sufficient elements in the list to make another key, so the loop breaks.
				break;
			}
		}
		
		// Save the order and pairs in case they will ever be used again - not likely.
		o = order;
		p = pairs;
	}
	
	// Generates a random word from a specific probability distribution.
	private String randomWord(ArrayList<WordCount> wcs)
	{
		ArrayList<String> possiblewords = new ArrayList<String>();
		// Make a new arraylist where each word is repeated the in the list the number of times it existed in the text (count).
		for (int i = 0; i < wcs.size(); i++)
		{
			WordCount wc = wcs.get(i);
			for (int j = 0; j < wc.count; j++)
			{
				possiblewords.add(wc.word);
			}
		}
		// Now just randomly select one of the words from that new list. This has correct probability.
		int rand = new Random().nextInt(possiblewords.size());
		return possiblewords.get(rand);
	}
	
	// Generates some (relatively gibberish) text based off of the probability matrix.
	public String[] genText(int numWords)
	{
		// Array for storing all of the words about to be made.
		String[] text = new String[numWords];
		if (numWords < 1) // Generating less than 1 word doesn't make sense.
		{
			return null;
		}
		// Get a RNG
		Random rand = new Random();
		// Get a starting word by choosing a random key-value pair and selecting the words of the key to start.
		int randint = rand.nextInt(p.size());
		String[] keys = p.get(randint).keys;
		for (int i = 0; i < keys.length; i++)
		{
			text[i]=keys[i];
		}
		// This means that text now has some words in it with that number of words being equal to the 'order' of the chain.
		// Therefore, the index of the text to be examined begins at the index of the last word added.
		int currentWordIndex = keys.length - 1;
		// The past index is the index of the last pair that was examined.
		int pastIndex = randint;
		for (int i = currentWordIndex; i < numWords - 1; i++)
		{
			String word = null;
			// Select the pair that previously was used.
			// This pair has the probability distribution that will be used to generate the next word.
			Pair curP = p.get(pastIndex);
			// So get a new random word.
			word = randomWord(curP.wordCounts);
			// and put it in the array at the next index.
			text[currentWordIndex + 1] = word;
			// Now we need to create a new pair based on the new state of the markov chain.
			String[] newKeys = new String[o];
			// Determine the new keys to look for. These new keys are just the new word with the past 'order' words preceding it.
			for (int j = currentWordIndex - o + 2; j < currentWordIndex + 2; j++)
			{
				newKeys[j - currentWordIndex + o - 2] = text[j];
			}
			// Next the past index is just set to the index of this new key. This new key is basically being found in the list
			// of all keys that existed in the text and then being given back its index.
			pastIndex=indexKeyExists(p, newKeys);
			// The index has to be increased.
			currentWordIndex++;
		}
		return text;
	}
	
	// Checks if a key exists in the pair array list. Returns the index if it does. Returns -1 if it doesn't.
	private int indexKeyExists(ArrayList<Pair> list, String[] key)
	{
		for (int i = 0; i < list.size(); i++)
		{
			String[] keyI = list.get(i).keys;
			if (StringsSame(keyI, key))
			{
				return i;
			}
		}
		return -1;
	}
	
	// Checks if two arrays of strings are the same.
	private boolean StringsSame (String[] s1, String[] s2)
	{
		boolean same = true;
		if (s1.length != s2.length || s1.length <= 0)
		{
			return false;
		}
		for (int i = 0; i < s1.length; i++)
		{
			if (!s1[i].equals(s2[i]))
			{
				same = false;
			}
		}
		return same;
	}
	
	// Gets the key that represents the Markov Chain key for the current index that is being examined.
	private String[] getKey(int order, int index, String[] elems)
	{
		// Literally looks at the array and pulls 'order' elements from it starting at the index to call the key.
		String[] key = new String[order];
		for (int i = 0; i < order; i++)
		{
			key[i] = elems[index + i];
		}
		return key;
	}
	
	// Creates a new pair of the specified order. Where index is the starting index for the set of keys.
	private Pair createPair(String[] elems, int order, int index)
	{
		// Find the keys for this probability dist.
		String[] keys = new String[order];
		for (int i = 0; i < order; i++)
		{
			keys[i] = elems[index + i];
		}
		// Create the pair using the set of keys and then the element that follows them.
		Pair p = new Pair(keys, elems[index + order]);
		return p;
	}
	
	// The maximum number of key-value pairs that exist in a chain that has an order specified by order
	// and created from a number of elements specified by length.
	private int maxNumPairs(int order, int length)
	{
		return length - order;
	}
	
	// If when iterating over the elements being used to make the chain, at the current index specified as index,
	// there exists another possible key-value pair to be made.
	private boolean hasNextKey(int order, int index, int length)
	{
		return (index + order) < length;
	}
	
	// Splits the text input by every space (" ")
	private String[] splitInput(String input)
	{
		String regex = "\\s+"; // Regular expression for spaces.
		return input.split(regex); // Split the input string at the regex i.e. separate by spaces.
	}
	
}
