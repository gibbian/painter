package com.thepainter;

import java.awt.Graphics2D;
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

import org.bytedeco.librealsense.motion_callback;
import org.opencv.core.Core;




public class App 
{

    static FileSystem sys = FileSystems.getDefault();
    static String filePath =  "img" + sys.getSeparator() +  "image.png";

    


    public static ArrayList<ArrayList<Integer>> tiles = new ArrayList<ArrayList<Integer>>();
    public static HashSet<ArrayList<Integer>> uniqueTiles = new HashSet<ArrayList<Integer>>();
    public static HashMap<String, ArrayList<Integer>> tileMap = new HashMap<String, ArrayList<Integer>>();

    public static void main( String[] args )
    {


        // Load image from file
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            System.out.println("Error loading image");
        }

        // Basic image info
        int resolution = 50;
        int mOrder = 20;
        int size = 10;
        int iteration = 0;

        // Generate images
        while(iteration < 10){
            System.out.print("\t\t\t\tIteration: " + iteration + "\r");
            System.out.flush();
            imageGenerationV3(img,resolution,mOrder,size, iteration);
            iteration++;
        }
        
        
    }

        // Xonverts an array of pixels into a png image.
        // pixels: The array of pixels to be converted
        // width: The width of the image
        // height: The height of the image
        // name: The name of the file that the image will be saved to

        // Return:
        // None

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


    public static void imageGenerationV3(BufferedImage img, int tileDimX, int mOrder, int size, int iteration){
        int tileDimY = tileDimX;
        tileImage(img, tileDimX, tileDimY);

        int num = 0;
        for(ArrayList<Integer> tile : uniqueTiles){
            tileMap.put(Integer.toString(num), tile);
            num++;
        }

        ArrayList<String> encodedTiles = encodeTiles(); // Encode the tiles

        MarkovGen<String> markov = new MarkovGen<String>(mOrder);

        //train
        markov.train(encodedTiles);

        
        ArrayList<String> newEncodedTiles = new ArrayList<String>();

        //generate
        int rows = size;
        for(int i = 0; i < 1; i++){
            System.out.print("rows: " + i + "/" + rows + "\r");
            System.out.flush();
            
            
            ArrayList<String> sequence = markov.generate(rows*rows);
            newEncodedTiles.addAll(sequence);
        }
        

        //stich the tiles together
        stitchTiles(newEncodedTiles, tileDimX, rows, iteration);


    }

    public static void stitchTiles(ArrayList<String> sequence, int tileWidth, int rows, int iteration){
        // iterate through the sequence, using the tileMap to get the tile for each element
        // stitch the tiles together
        // print the image

        // tile width = 100
        // sequence.size() = 4
        // total pixels = 4000

        int width = tileWidth * rows;
        if(width > 3000){
            width = 3000;
        }

        ArrayList<Integer> pixels = new ArrayList<Integer>();
        BufferedImage img = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);

        // use the tileMap to get the pixels for each tile and write them to the image
        int i = 0;

        for(int x = 0; x < rows; x++){
            for(int y = 0; y < rows; y++){
                if(x+y >= sequence.size()){
                    System.out.println("Error x+y: " + (x+y));
                    break;
                }
                
                ArrayList<Integer> tile = tileMap.get(sequence.get(x+y));
                for(int j = 0; j < tileWidth; j++){
                    for(int k = 0; k < tileWidth; k++){
                        if(x*tileWidth + j < width && y*tileWidth + k < width){
                            if(j*tileWidth + k < tile.size())
                                img.setRGB(x*tileWidth + j, y*tileWidth + k, tile.get(j * tileWidth + k));
                        }
                    }
                }

            }
        }




        try {
            ImageIO.write(img, "png", new File("img" + sys.getSeparator() + "gen" + sys.getSeparator() + "output" + iteration + ".png"));
        } catch (IOException e) {
            System.out.println("Error writing image");
        }
    }

    // Rotates an image by a random angle
    public static BufferedImage rotate(BufferedImage bimg) {
        int w = bimg.getWidth();
        int h = bimg.getHeight();
        double angle = Math.random() * 360;
        BufferedImage rotated = new BufferedImage(w, h, bimg.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
        graphic.drawImage(bimg, null, 0, 0);
        graphic.dispose();
        return rotated;
    }

    
    // Returns an arraylist of tiles from an image
    public static ArrayList<String> encodeTiles(){
        // System.out.println("Encoding Tiles...");

        // Create a reverse mapping from value list to key
        HashMap<ArrayList<Integer>, String> reverseTileMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<Integer>> entry : tileMap.entrySet()) {
            reverseTileMap.put(entry.getValue(), entry.getKey().toString());
        }

        ArrayList<String> encodedTiles = new ArrayList<>();
        int tilesSize = tiles.size();
        for (int i = 0; i < tilesSize; i++) {
            // Update progress less frequently
            // if (i % 100 == 0) { // Change the value according to the data size
            //     System.out.print(i + "/" + tilesSize + "\r");
            // }

            ArrayList<Integer> tile = tiles.get(i);
            
            // Look up the key in the reverse mapping
            String encodedTile = reverseTileMap.get(tile);
            if (encodedTile != null) {
                encodedTiles.add(encodedTile);
            }
        }
        // System.out.println();
        return encodedTiles;
    }

    // Prints an image from an arraylist of encoded tiles
    public static void printImageFromEncodedTiles(ArrayList<String> encodedTiles, HashMap<String, ArrayList<Integer>> tileMap, int width, int height, int tileDimX, int tileDimY, String name){
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        int i = 0;

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(i >= encodedTiles.size()){
                    break;
                }
                ArrayList<Integer> tile = tileMap.get(encodedTiles.get(i));
                for(int j = 0; j < tileDimX; j++){
                    for(int k = 0; k < tileDimY; k++){
                        if(x + j < width && y + k < height){
                            if(j*tileDimX + k < tile.size())
                                img.setRGB(x + j, y + k, tile.get(j * tileDimX + k));
                        }
                    }
                }
                i++;
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

    // Returns an arraylist of tiles from an image
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
