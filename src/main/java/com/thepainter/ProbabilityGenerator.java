package com.thepainter;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.sound.midi.SysexMessage;

public class ProbabilityGenerator <E>
{
	ArrayList<E> tokens;
	ArrayList<Float> symbolsCount;
	ArrayList<Double> symbolsProbability;
	int numTokens;

	//nested convenience class to return three arrays from sortArrays() method
	//students do not need to use this class
	protected class SortArraysOutput
	{
		public ArrayList<E> symbolsListSorted;
		public ArrayList<Float> symbolsCountSorted;
		public ArrayList<Double> symbolsProbabilitySorted;
	}

	// Trains the generator by collecting statistics about the given midi file
	public void train(ArrayList<E> data)
	{
		// Create a list of tokens and a list of counts of those tokens
		if(tokens == null || symbolsCount == null){
			tokens = new ArrayList<E>();
			symbolsCount = new ArrayList<Float>();
			numTokens = 0;
		}

		// Loop through the data and count the number of times each token is seen
		// Store the tokens and counts in the two lists declared above
		for(E e : data){
			if(tokens.contains(e)){
				int index = tokens.indexOf(e);
				symbolsCount.set(index, symbolsCount.get(index) + 1.0f);
			}
			else{
				tokens.add(e);
				symbolsCount.add(1.0f);
			}
		}
		numTokens += data.size();

		calculateProbabilities();

		// Print out the tokens and counts
		//System.out.println(tokens.toString() + "\t" + symbolsCount.toString() + "\t" + numTokens);
	}


	// Fill the global array with the probability of each token, adding a third array to the parallel array lists
	public void calculateProbabilities(){
		symbolsProbability = new ArrayList<Double>();
		for(Float f : symbolsCount){
			symbolsProbability.add((double)f / numTokens);
		}
	}


	//sort the symbols list and the counts list, so that we can easily print the probability distribution for testing
	//symbols -- your alphabet or list of symbols (input)
	//counts -- the number of times each symbol occurs (input)
	//probablities -- the probability of each symbol (intput)
	//symbolsListSorted -- your SORTED alphabet or list of symbols (output)
	//symbolsCountSorted -- list of the number of times each symbol occurs inorder of symbolsListSorted  (output)
	//probablityListSorted -- the probability of each symbol (output)
	public SortArraysOutput sortArrays(ArrayList<E> symbols, ArrayList<Float> counts, ArrayList<Double> probablities)	{

		SortArraysOutput sortArraysOutput = new SortArraysOutput();
		
		sortArraysOutput.symbolsListSorted = new ArrayList<E>(symbols);
		sortArraysOutput.symbolsCountSorted = new ArrayList<Float>();
		sortArraysOutput.symbolsProbabilitySorted = new ArrayList<Double>();
		
		//sort the symbols list
		Collections.sort(sortArraysOutput.symbolsListSorted, new Comparator<E>() {
			@Override
			public int compare(E o1, E o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		//use the current sorted list to reference the counts and get the sorted counts
		for(int i=0; i<sortArraysOutput.symbolsListSorted.size(); i++)
		{
			int index = symbols.indexOf(sortArraysOutput.symbolsListSorted.get(i));
			sortArraysOutput.symbolsCountSorted.add(counts.get(index));
			sortArraysOutput.symbolsProbabilitySorted.add(probablities.get(index));

		}

		return sortArraysOutput;

	}
	
	//Students should USE this method in your unit tests to print the probability distribution
	//HINT: you can overload this function so that it uses your class variables instead of taking in parameters
	//boolean is FALSE to test train() method & TRUE to test generate() method
	//symbols -- your alphabet or list of symbols (input)
	//counts -- the number of times each symbol occurs (input)
	//sumSymbols -- the count of how many tokens we have encountered (input)
	public void printProbabilityDistribution(boolean round, ArrayList<E> symbols, ArrayList<Float> counts, double sumSymbols)
	{
		//sort the arrays so that elements appear in the same order every time and it is easy to test.
		SortArraysOutput sortResult = sortArrays(symbols, counts, symbolsProbability);
		ArrayList<E> symbolsListSorted = sortResult.symbolsListSorted;
		ArrayList<Double> symbolsProbabilitySorted = sortResult.symbolsProbabilitySorted;
		System.out.println("-----Probability Distribution-----");
		
		for (int i = 0; i < symbols.size(); i++)
		{
			double probability = symbolsProbabilitySorted.get(i);
			if (round){
				DecimalFormat df = new DecimalFormat("#.##");
				System.out.println("Data: " + symbolsListSorted.get(i) + " | Probability: " + df.format(probability));
			}
			else
			{
				System.out.println("Data: " + symbolsListSorted.get(i) + " | Probability: " + (probability));
			}
		}
		System.out.println("------------");
	}

	//Overloaded method to print the probability distribution using class variables
	public void printProbabilityDistribution(boolean round){
		printProbabilityDistribution(round, tokens, symbolsCount, numTokens);
	}

	/*
	 * 	Generates a new ArrayList of symbols based on the statistics collected in the train() method
	 * 		selectRandomSymbol() is used to select the next symbol
	 * 	generationSize -- the number of symbols to generate
	 * 	returns the generated ArrayList
	 */
	public ArrayList<E> generate(int generationSize) {
		ArrayList<E> output = new ArrayList<E>();
		for(int i = 0; i < generationSize; i++){
			output.add(selectRandomSymbol());
		}
		return output;
	}

	/* 	Selects a random symbol based on the probability distribution
	*	 First we generating a random decimal, then begin interating through the probability distribution,
	*    if the random decimal is less than the current probability, we select that symbol,
	*	 otherwise we subtract the current probability from the random decimal and continue.
	*	 If we reach the end of the probability distribution (output == null), we select the last symbol since we reached the end of the distribution.
	*/
	public E selectRandomSymbol(){
		Double randy = Math.random();
		E output = null;
		for(int i = 0; i < symbolsProbability.size(); i++){
			Double d = symbolsProbability.get(i);
			if(randy < d){
				output = tokens.get(i);
				break;
			}
			else{
				randy -= d;
			}
		}
		if(output == null){
			output = tokens.get(tokens.size() - 1);
		}
		return output;
	}

}
