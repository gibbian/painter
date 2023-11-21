package com.thepainter;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class MarkovGen<T> {

    HashMap<ArrayList<T>, HashMap<T,Integer>> transitionMap = new HashMap<ArrayList<T>, HashMap<T,Integer>>();
    HashMap<ArrayList<T>, HashMap<T,Double>> transitionProb = new HashMap<ArrayList<T>, HashMap<T,Double>>();
    int mOrder = 1;

    public MarkovGen() {
        mOrder = 1;
    }

    public MarkovGen(int _mOrder) {
        mOrder = _mOrder;
    }

    public void delete(){
        transitionMap = null;
        transitionProb = null;
    }


    //Markov Train
    public void train(ArrayList<T> data) {
        for (int i = 0; i <= data.size() - mOrder - 1; i++) {
            ArrayList<T> sequence = new ArrayList<T>(data.subList(i, i + mOrder));
            T nextToken = data.get(i + mOrder);
    
            if (!transitionMap.containsKey(sequence)) {
                transitionMap.put(sequence, new HashMap<T, Integer>());
            }
            
            Map<T, Integer> sequenceCounts = transitionMap.get(sequence);
            
            // Updating counts without redundant put back into transitionMap
            if(sequenceCounts.containsKey(nextToken)) {
                sequenceCounts.put(nextToken, sequenceCounts.get(nextToken) + 1);
            } else {
                sequenceCounts.put(nextToken, 1);
            }
        }
    
        // Fill transition probability map
        for (Map.Entry<ArrayList<T>, HashMap<T, Integer>> entry : transitionMap.entrySet()) {
            ArrayList<T> sequence = entry.getKey();
            HashMap<T, Integer> sequenceCounts = entry.getValue();
            HashMap<T, Double> sequenceProb = new HashMap<T, Double>();
            
            int total = 0;
            for (Integer count : sequenceCounts.values()) {
                total += count;
            }
            for (Map.Entry<T, Integer> countEntry : sequenceCounts.entrySet()) {
                sequenceProb.put(countEntry.getKey(), (double) countEntry.getValue() / total);
            }
            transitionProb.put(sequence, sequenceProb);
        }
    }
    // public void train(ArrayList<T> data){
    //     for(int i = 0; i <= data.size() - mOrder - 1; i++){
    //         // System.out.print("Training:");
    //         // System.out.print(i + "/" + data.size() + "\r") ;
    //         // System.out.flush();

    //         ArrayList<T> sequence = new ArrayList<T>();
    //         for(int j = 0; j < mOrder; j++){
    //             sequence.add(data.get(i + j));
    //         }

    //         T nextToken = data.get(i + mOrder);

    //         transitionMap.putIfAbsent(sequence, new HashMap<T, Integer>());
    //         Map<T, Integer> sequenceCounts = transitionMap.get(sequence);
            
    //         if(sequenceCounts.containsKey(nextToken)){
    //             sequenceCounts.put(nextToken, sequenceCounts.get(nextToken) + 1);
    //         } else {
    //             sequenceCounts.put(nextToken, 1);
    //         }

    //         transitionMap.put(sequence, (HashMap<T, Integer>) sequenceCounts);
    //     }

    //     //Fill transitionProb

    //     for(ArrayList<T> sequence : transitionMap.keySet()){
    //         HashMap<T, Integer> sequenceCounts = transitionMap.get(sequence);
    //         HashMap<T, Double> sequenceProb = new HashMap<T, Double>();
    //         int total = 0;
    //         for(T token : sequenceCounts.keySet()){
    //             total += sequenceCounts.get(token);
    //         }
    //         for(T token : sequenceCounts.keySet()){
    //             sequenceProb.put(token, (double) sequenceCounts.get(token) / total);
    //         }
    //         transitionProb.put(sequence, sequenceProb);
    //     }

        
    // }

    //Generate
    // public ArrayList<T> generate(int size){
    //     ArrayList<T> output = new ArrayList<T>();
    //     //Randomly select first sequence
    //     ArrayList<T> sequence = new ArrayList<T>();
        
    //     int randy = (int)(Math.random() * (transitionProb.keySet().size() - 1));
    //     sequence = (ArrayList<T>) transitionProb.keySet().toArray()[randy];

    //     output.addAll(sequence);
    //     size -= sequence.size();

    //     for(int i = 0; i < size; i++){
            
    //         HashMap<T, Double> sequenceProb = transitionProb.get(sequence);
    //         if(sequenceProb != null){
    //             double rand = Math.random();
    //             double total = 0;
    //             for(T token : sequenceProb.keySet()){
    //                 total += sequenceProb.get(token);
    //                 if(rand <= total){
    //                     output.add(token);
    //                     break;
    //                 }
    //             }
    //         }
    //         else{
    //             // If sequenceProb is null, randomly select a new sequence
    //             randy = (int)(Math.random() * (transitionProb.keySet().size() - 1));
                
    //             i--;
    //             continue;
    //         }
    //         sequence.remove(0);
    //         sequence.add(output.get(output.size() - 1));
    //     }
    //     return output;
    // }

    public ArrayList<T> generate(int size){
        ArrayList<T> output = new ArrayList<T>();
        // Store keys in a list for faster access
        List<ArrayList<T>> keys = new ArrayList<ArrayList<T>>(transitionProb.keySet());
        

        //Randomly select first sequence
        ArrayList<T> sequence = keys.get((int)(Math.random() * keys.size()));
        
        output.addAll(sequence);
        size -= sequence.size();
    
        for(int i = 0; i < size; i++){
            
            HashMap<T, Double> sequenceProb = transitionProb.get(sequence);
            if(sequenceProb != null){
                double rand = Math.random();
                double total = 0;
                for(T token : sequenceProb.keySet()){
                    total += sequenceProb.get(token);
                    if(rand <= total){
                        output.add(token);
                        break;
                    }
                }
            }
            else{
                // If sequenceProb is null, randomly select a new sequence
                // sequence = keys.get((int)(Math.random() * keys.size()));
                // i--;
                continue;
            }
            sequence.remove(0);
            sequence.add(output.get(output.size() - 1));
        }
    
        return output;
    }
    


    //Print Map
    public void printTransitionMap(){
        for(ArrayList<T> sequence : transitionMap.keySet()){
            System.out.print(sequence.toString() + " : ");
            for(T token : transitionMap.get(sequence).keySet()){
                System.out.print("(" + token + ") " + transitionMap.get(sequence).get(token) + " ");
            }
            System.out.println();
        }
    }
    //Print Prob
    public void printTransitionProb(){
        for(ArrayList<T> sequence : transitionProb.keySet()){
            System.out.print(sequence.toString() + " : ");
            for(T token : transitionProb.get(sequence).keySet()){
                System.out.print("(" + token + ") " + transitionProb.get(sequence).get(token) + " ");
            }
            System.out.println();
        }
    }


    
}