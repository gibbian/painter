package com.thepainter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.opencv.core.Core;




/**
 * Hello world!
 *
 */
public class App 
{

    static FileSystem sys = FileSystems.getDefault();
    static String filePath =  "img" + sys.getSeparator() +  "paint.jpeg";


    public static ArrayList<ArrayList<Integer>> tiles = new ArrayList<ArrayList<Integer>>();
    public static HashSet<ArrayList<Integer>> uniqueTiles = new HashSet<ArrayList<Integer>>();
    public static HashMap<String, ArrayList<Integer>> tileMap = new HashMap<String, ArrayList<Integer>>();

    public static void main( String[] args )
    {
        //Delete all files in img/tiles
        // File tilesDir = new File("img" + sys.getSeparator() + "tiles");
        // for(File file : tilesDir.listFiles()){
        //     file.delete();
        // }

        
        

        // Load image from file
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            System.out.println("Error loading image");
        }

        
        imageGenerationV2(img, 1, 1, 2);
        
        
    }

    public static void printImage(ArrayList<Integer> pixels, int width, int height, String name){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int i = 0;

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(i >= pixels.size()){
                    break;
                }
                if(pixels.get(i) == null){
                    img.setRGB(x, y, 0);
                }
                else{
                    img.setRGB(x, y, pixels.get(i));
                }
                i++;
            }
        }
        try {
            ImageIO.write(img, "png", new File(name + ".png"));
        } catch (IOException e) {
            System.out.println("Error writing image");
        }
    }

    public static <E> ArrayList<E> tileSort(ArrayList<E> data, int width, int height, int tileWidth, int tileHeight){
        ArrayList<E> newData = new ArrayList<E>();
        int tilesX = width / tileWidth;
        int tilesY = height / tileHeight;
        for(int x = 0; x < tilesX; x++){
            for(int y = 0; y < tilesY; y++){
                for(int i = 0; i < tileWidth; i++){
                    for(int j = 0; j < tileHeight; j++){
                        int index = (y * tileHeight + j) * width + (x * tileWidth + i);
                        if(index < data.size()){
                            newData.add(data.get(index));
                        }
                    }
                }
            }
        }
        return newData;
    }

    public static <E> ArrayList<E> undoTileSort(ArrayList<E> data, int width, int height, int tileWidth, int tileHeight){
        ArrayList<E> oldData = new ArrayList<E>();
        for(int i = 0; i < width * height; i++){
            oldData.add(null);
        }
        int tilesX = width / tileWidth;
        int tilesY = height / tileHeight;
        int index = 0;
        for(int x = 0; x < tilesX; x++){
            for(int y = 0; y < tilesY; y++){
                for(int i = 0; i < tileWidth; i++){
                    for(int j = 0; j < tileHeight; j++){
                        if(index < data.size()){
                            int oldIndex = (y * tileHeight + j) * width + (x * tileWidth + i);
                            oldData.set(oldIndex, data.get(index++));
                        }
                    }
                }
            }
        }
        return oldData;
    }


    public static void imageGenerationV1(BufferedImage img){
        ArrayList<Integer> pixels = new ArrayList<Integer>();
        //read pixels from image
        for(int x = 0; x < img.getWidth(); x++){
            for(int y = 0; y < img.getHeight(); y++){
                pixels.add(img.getRGB(x, y));
            }
        }

        double res = 5;

        int tileDimX = (int)(10 *res);
        int tileDimY = (int)(10 *res);
        
        
        pixels = tileSort(pixels, img.getWidth(), img.getHeight(), tileDimX, tileDimY);

        int i = 0;
        while(true){
            MarkovGen<Integer> markov = new MarkovGen<Integer>((int)(100 * res));
            
            markov.train(pixels);

            int width = 600;
            int height = 300;

            ArrayList<Integer> newPixels = new ArrayList<Integer>();
            
            newPixels = markov.generate(width * height);
            newPixels = undoTileSort(newPixels, width, height, tileDimX, tileDimY);

            printImage(newPixels, width, height, "output");
            System.out.print(i + "\r");
            System.out.flush();
            i++;
        }
    }

    public static void imageGenerationV2(BufferedImage img, int tileDimX, int tileDimY, int mOrder){
        System.out.println("\tReading Image: Width: " + img.getWidth() + " Height: " + img.getHeight());
        System.out.println("\tTile Dimensions: Width: " + tileDimX + " Height: " + tileDimY);


        
        //divide image into tiles
        System.out.println("Splitting Image into Tiles...");
        System.out.flush();
        tileImage(img, tileDimX, tileDimY);
        System.out.println("Tiles Size: " + tiles.size());
        System.out.println("Unique Tiles Size: " + uniqueTiles.size());

        //System.out.println("Scanning Unique Tiles...");
        //ArrayList<ArrayList<Integer>> uniqueTiles = scanUniqueTiles(tiles);

        
        //for each unique tile in the HashSet, add it to the tileMap with an incrimenting number as the key
        System.out.println("Creating Tile Map...");
        int num = 0;
        for(ArrayList<Integer> tile : uniqueTiles){
            System.out.print(num + "/" + uniqueTiles.size() + "\r");
            System.out.flush();
            tileMap.put(Integer.toString(num), tile);
            num++;
        }


        ArrayList<String> encodedTiles = encodeTiles(); // Encode the tiles

        MarkovGen<String> markov = new MarkovGen<String>(mOrder);

    
        markov.train(encodedTiles);

        for(int iterator = 0; iterator < 100; iterator++){
            
            // Create a new MarkovGen object (with order 1)
            
            //wait for half a second
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Error sleeping");
            }
            
            //4032 3034
            // int width = 600;
            // int height = 300;

            //2040 1460
            int width = 500;
            int height = 500;
            ArrayList<String> newEncodedTiles = new ArrayList<String>();
            for(int i = 0; i < height; i++){
                System.out.print(i + "/" + height + "\r");
                System.out.flush();
                ArrayList<String> sequence = markov.generate(width);
                newEncodedTiles.addAll(sequence);
            }
            

            printImageFromEncodedTiles(newEncodedTiles, tileMap, width, height, tileDimX, tileDimY, "output" + iterator);
            encodedTiles = newEncodedTiles;
        }
        
    }

    public static ArrayList<String> encodeTiles(){
        System.out.println("Encoding Tiles...");

        // Create a reverse mapping from value list to key
        HashMap<ArrayList<Integer>, String> reverseTileMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : tileMap.entrySet()) {
            reverseTileMap.put(entry.getValue(), entry.getKey().toString());
        }

        ArrayList<String> encodedTiles = new ArrayList<>();
        int tilesSize = tiles.size();
        for (int i = 0; i < tilesSize; i++) {
            // Update progress less frequently
            if (i % 100 == 0) { // Change the value according to the data size
                System.out.print(i + "/" + tilesSize + "\r");
            }

            ArrayList<Integer> tile = tiles.get(i);
            
            // Look up the key in the reverse mapping
            String encodedTile = reverseTileMap.get(tile);
            if (encodedTile != null) {
                encodedTiles.add(encodedTile);
            }
        }
        System.out.println();
        return encodedTiles;
    }

    public static void printImageFromEncodedTiles(ArrayList<String> encodedTiles, HashMap<String, ArrayList<Integer>> tileMap, int width, int height, int tileDimX, int tileDimY, String name){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int i = 0;

        //System.out.println("Printing Image...");
        for(int x = 0; x < width; x += tileDimX){
            System.out.print(x + "/" + width + "\r");
            System.out.flush();
            for(int y = 0; y < height; y += tileDimY){
                if(i >= encodedTiles.size()){
                    break;
                }
                ArrayList<Integer> tile = tileMap.get(encodedTiles.get(i));
                for (int j = 0; j < tileDimX; j++) {
                    for (int k = 0; k < tileDimY; k++) {
                        int newX = x + j;
                        int newY = y + k;
                        if (newX < width && newY < height) {
                            img.setRGB(newX, newY, tile.get(j * tileDimX + k));
                        }
                    }
                }
                i++;
            }
        }

        //blur image
        //System.out.println("Blurring Image...");
        //System.out.flush();

        int blurRadius = 0;

        for(int x = 0; x < width; x++){
            System.out.print(x + "/" + width + "\r");
            System.out.flush();
            for(int y = 0; y < height; y++){
                ArrayList<Integer> pixels = new ArrayList<Integer>();
                for(int iX = -blurRadius; iX <= blurRadius; iX++){
                    for(int iY = -blurRadius; iY <= blurRadius; iY++){
                        int newX = x + iX;
                        int newY = y + iY;
                        if(newX >= 0 && newX < width && newY >= 0 && newY < height){
                            pixels.add(img.getRGB(newX, newY));
                        }
                    }
                }
                int r = 0;
                int g = 0;
                int b = 0;
                for(int j = 0; j < pixels.size(); j++){
                    r += (pixels.get(j) >> 16) & 0xFF;
                    g += (pixels.get(j) >> 8) & 0xFF;
                    b += (pixels.get(j)) & 0xFF;
                }
                r /= pixels.size();
                g /= pixels.size();
                b /= pixels.size();
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }
        try {
            ImageIO.write(img, "png", new File("img" + sys.getSeparator() + "gen" + sys.getSeparator() + "output.png"));
        } catch (IOException e) {
            System.out.println("Error writing image");
        }
    } 

    // Returns an arraylist of tiles from an image
    public static void tileImage(BufferedImage img, int tileDimX, int tileDimY){
        tiles = new ArrayList<ArrayList<Integer>>();

        for(int x = 0; x < img.getWidth(); x += tileDimX){
            System.out.print(x + "/" + img.getWidth() + "\r");
            System.out.flush();
            for(int y = 0; y < img.getHeight(); y += tileDimY){
                ArrayList<Integer> tile = new ArrayList<Integer>();
                for(int i = 0; i < tileDimX; i++){
                    for(int j = 0; j < tileDimY; j++){
                        if(x + i < img.getWidth() && y + j < img.getHeight()){
                            tile.add(img.getRGB(x + i, y + j));
                        }
                    }
                }
                tiles.add(tile);
                uniqueTiles.add(tile);
            }
        }
    }

    public static ArrayList<ArrayList<Integer>> scanUniqueTiles(ArrayList<ArrayList<Integer>> tiles){
         //Remove Duplicate Tiles
        ArrayList<ArrayList<Integer>> uniqueTiles = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i < tiles.size(); i++){
            System.out.print(i + "/" + tiles.size() + "\r");
            System.out.flush();
            if(!uniqueTiles.contains(tiles.get(i))){
                uniqueTiles.add(tiles.get(i));
            }

        }
        return uniqueTiles;
    }

}
