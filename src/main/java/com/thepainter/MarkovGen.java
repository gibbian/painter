package com.thepainter;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

class MarkovGen<T> {
    HashMap<ArrayList<T>, HashMap<T, Integer>> transitionMap = new HashMap<ArrayList<T>, HashMap<T, Integer>>();
    Random random = new Random();
    int mOrder = 1;

    public MarkovGen() {
        this(1);
    }

    public MarkovGen(int _mOrder) {
        mOrder = _mOrder;
    }

    public void train(ArrayList<T> data) {
        if (data.size() < mOrder + 1) {
            // Not enough data to train
            return;
        }

        // Iterate through the data tokens
        for (int i = 0; i < data.size() - mOrder; i++) {
            ArrayList<T> currentSequence = new ArrayList<T>(data.subList(i, i + mOrder));
            T nextToken = data.get(i + mOrder);

            // Get or create the transitions map for the current sequence
            HashMap<T, Integer> sequenceTransitions = transitionMap.getOrDefault(currentSequence, new HashMap<T, Integer>());

            // Update the frequency count for this sequence to nextToken transition
            int oldCount = sequenceTransitions.getOrDefault(nextToken, 0);
            sequenceTransitions.put(nextToken, oldCount + 1);

            // Update the transition map
            transitionMap.put(currentSequence, sequenceTransitions);
        }
        //System.out.println("Train Complete");
    }

    public ArrayList<T> generate(int size) {
        if (size <= mOrder) {
            throw new IllegalArgumentException("Size must be greater than mOrder.");
        }

        ArrayList<T> result = new ArrayList<>();
        ArrayList<T> currentSequence = chooseStartingSequence();

        if (currentSequence == null || currentSequence.isEmpty()) {
            // Cannot generate a chain without a starting sequence
            return result;
        }

        result.addAll(currentSequence);

        // Generate subsequent tokens
        for (int i = mOrder; i < size; i++) {
            System.out.print("\t\tgen: " + i + "/" + size + "\r");
            System.out.flush();

            T nextToken = nextTokenWithFallback(currentSequence);
            //generateSingleToken(currentSequence);
            if (nextToken == null) {
                // Cannot generate next token, possibly due to reaching a terminal state in the chain
                break;
            }
            result.add(nextToken);
            // Shift the sequence window to include the next token as the sequence advances
            currentSequence.remove(0);
            currentSequence.add(nextToken);
        }

        return result;
    }

    private ArrayList<T> chooseStartingSequence() {
        // Choose a starting sequence at random or based on a specific criterion
        List<ArrayList<T>> keys = new ArrayList<>(transitionMap.keySet());
        if (keys.isEmpty()) {
            return null; // No starting sequence available
        }
        return keys.get(random.nextInt(keys.size()));
    }

    private T generateSingleToken(ArrayList<T> currentSequence) {
        HashMap<T, Integer> sequenceTransitions = transitionMap.get(currentSequence);
        if (sequenceTransitions == null || sequenceTransitions.isEmpty()) {
            // No transitions available for current sequence
            return null;
        }

        // Calculate total sum of transition frequencies
        int total = 0;
        for (Integer count : sequenceTransitions.values()) {
            total += count;
        }
        
        // Pick a random threshold within the total transitions
        int threshold = random.nextInt(total);
        int cumulativeSum = 0;

        // Iterate over transitions and select the next token based on the accumulated weights
        for (Map.Entry<T, Integer> entry : sequenceTransitions.entrySet()) {
            cumulativeSum += entry.getValue();
            if (threshold < cumulativeSum) {
                return entry.getKey();
            }
        }

        // Should not reach here if logic is correct
        return null;
    }

    private T nextTokenWithFallback(ArrayList<T> currentSequence) {
        T nextToken = generateSingleToken(currentSequence);
        if (nextToken != null) {
            return nextToken;
        }

        // Attempt to regenerate by shortening the sequence from the front
        for (int i = 1; i < currentSequence.size(); i++) {
            
            ArrayList<T> subSequence = new ArrayList<>(currentSequence.subList(i, currentSequence.size()));
            nextToken = generateSingleToken(subSequence);
            if (nextToken != null) {
                return nextToken;
            }
        }

        // As a final fallback, pick a token at random from the entire set of keys
        return pickRandomToken();
    }

    private T pickRandomToken() {
        // Flatten all the keys to a single list of tokens from which to pick
        Set<T> allPossibleTokens = new HashSet<>();
        for (Entry<ArrayList<T>, HashMap<T, Integer>> entry : transitionMap.entrySet()) {
            allPossibleTokens.addAll(entry.getKey()); // Add all tokens from the sequences
            allPossibleTokens.addAll(entry.getValue().keySet()); // Add possible next tokens
        }

        // Convert the set to a list for random access
        List<T> tokensList = new ArrayList<>(allPossibleTokens);
        if (tokensList.isEmpty()) {
            return null; // No tokens available to pick from
        }

        // Randomly pick a token from the list
        return tokensList.get(random.nextInt(tokensList.size()));
    }
}
