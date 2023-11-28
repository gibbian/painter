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

        
      // imageGenerationV2(img, 1, 1, 1);
        imageGenerationV3(img,1,1);
        
        
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

    public static void imageGenerationV3(BufferedImage img, int tileDimX, int tileDimY){
        System.out.println("Reading Image...\nWidth: " + img.getWidth() + "\nHeight: " + img.getHeight());
        
        System.out.println("Splitting Image into Tiles...");
        System.out.flush();
        tileImage(img, tileDimX, tileDimY);
        System.out.println("Tiles Size: " + tiles.size());
        System.out.println("Unique Tiles Size: " + uniqueTiles.size());

        System.out.println("Creating Tile Map...");
        int num = 0;
        for(ArrayList<Integer> tile : uniqueTiles){
            System.out.print(num + "/" + uniqueTiles.size() + "\r");
            System.out.flush();
            tileMap.put(Integer.toString(num), tile);
            num++;
        }

        ArrayList<String> encodedTiles = encodeTiles(); // Encode the tiles

        MarkovGen<String> markov = new MarkovGen<String>(1);

        markov.oneTrain(encodedTiles);

        
        ArrayList<String> newEncodedTiles = new ArrayList<String>();
        for(int i = 0; i < 10000; i++){
            System.out.print(i + "/" + 10000 + "\r");
            System.out.flush();
            ArrayList<String> sequence = markov.oneGen(10000);
            newEncodedTiles.addAll(sequence);
        }

        System.out.println("sequence size: " + newEncodedTiles.size());

        printImageFromEncodedTiles(newEncodedTiles, tileMap, 1000, 1000, tileDimX, tileDimY, "output");
        
        




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
