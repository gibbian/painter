package com.thepainter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

class MarkovGen<T> {

    HashMap<ArrayList<T>, HashMap<T,Integer>> transitionMap = new HashMap<ArrayList<T>, HashMap<T,Integer>>();
    HashMap<ArrayList<T>, HashMap<T,Double>> transitionProb = new HashMap<ArrayList<T>, HashMap<T,Double>>();

    HashMap<T, HashMap<T, Integer>> transitionMapOne = new HashMap<T, HashMap<T, Integer>>();
    HashMap<T, HashMap<T, Double>> transitionProbOne = new HashMap<T, HashMap<T, Double>>();

    int mOrder = 1;

    Random random = new Random();

    ProbabilityGenerator<T> pg;

    public MarkovGen() {
        mOrder = 1;
    }

    public MarkovGen(int _mOrder) {
        mOrder = _mOrder;
    }

    public void oneTrain(ArrayList<T> data){
        // Iterate through the data tokens
        for (int i = 0; i < data.size() - 1; i++) {
            T currentToken = data.get(i);
            T nextToken = data.get(i + 1);

            // Get the next transitions map for the current token,
            // or create it if it doesn't exist
            HashMap<T, Integer> nextTransitions = transitionMapOne.getOrDefault(currentToken, new HashMap<T, Integer>());
            
            // Update the frequency count for the transition from currentToken to nextToken
            int oldCount = nextTransitions.getOrDefault(nextToken, 0);
            nextTransitions.put(nextToken, oldCount + 1);

            // Update the transitions map
            transitionMapOne.put(currentToken, nextTransitions);
        }
        System.out.println("One Train Complete");
        List<ArrayList<T>> keys = new ArrayList<ArrayList<T>>(transitionProb.keySet());
        System.out.println("Markov Train Complete:" + keys.size());
    }

    public ArrayList<T> oneGen(int size){
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive.");
        }

        ArrayList<T> result = new ArrayList<>();
        T currentToken = chooseStartingToken();

        if (currentToken == null) {
            // Cannot generate a chain without a starting token 
            return result;
        }

        result.add(currentToken);

        // Generate subsequent tokens
        for (int i = 1; i < size; i++) {
            T nextToken = generateSingleToken(currentToken);
            if (nextToken == null) {
                // Cannot generate next token, possibly due to reaching a terminal state in the chain
                break;
            }
            result.add(nextToken);
            currentToken = nextToken;
        }

        return result;
    }

    private T chooseStartingToken() {
        // Choose a starting token at random or based on a specific criterion
        List<T> keys = new ArrayList<>(transitionMapOne.keySet());
        if (keys.isEmpty()) {
            return null; // No starting point available
        }
        return keys.get(random.nextInt(keys.size()));
    }

    private T generateSingleToken(T currentToken) {
        HashMap<T, Integer> currentTransitions = transitionMapOne.get(currentToken);
        if (currentTransitions == null || currentTransitions.isEmpty()) {
            // No transitions available for current token
            return null;
        }

        int total = 0;
        for (Integer count : currentTransitions.values()) {
            total += count;
        }
        int choice = random.nextInt(total); // Pick a random number in the total range
        int cumulative = 0;

        for (Map.Entry<T, Integer> entry : currentTransitions.entrySet()) {
            cumulative += entry.getValue();
            if (choice < cumulative) {
                return entry.getKey(); // Return the chosen token based on its weighted probability
            }
        }

        // Should not reach here if logic is correct
        return null;
    }




    
}