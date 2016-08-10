[Markov Chains](https://en.wikipedia.org/wiki/Markov_chain) are statistical models used to represent systems that undergo random state changes where the probabilities of transitions are based on the current state of the chain. This project uses that idea and applies it to words and sentences. Using a large corpus of text, e.g. Shakespeare's works, a novel, news articles, etc..., the program builds a Markov Chain that models which words come after each other. 

For example, in the (uncapitalized) sentence `the dog at the food`, the Markov Chain looks at pairs of words like `the dog` and `the food` and then would determine that if the current word is `the` there is a 50% probably of it being followed by `dog` and a 50% probably of it being followed by `food`. Now imagine repeating this process across and entire novel. What one obtains is a mapping of every word in the corpus to a map of probabilities of which other words will follow it.

Then, the Markov Chain is used to create chains of words by first picking a random starting word in the corpus and then randomly selecting a word to follow it using the probabilities laid out in the Markov Chain.

This project also allows for implementing Markov Chains that aren't completely memoryless. A memoryless Markov Chain for generating text would only look at the last 1 word when generating the new one. This project allows for looking at the past n words. However, performance decreases as n increases.

For example, using *20,000 Leagues Under the Sea* as the corpus, these are some of the sentences that the Chain creates:

* These books, Professor, are at your disposal.
* Captain Baker thought at first struck by a thunderbolt.
* It was evidently propelled by her own momentum.
* But the idea of seizing the Nautilus towards the Island of Crespo.