package com.sdis;

public class MulticastThread extends Thread{
    String message;

    public MulticastThread(){

    }

    public void run() {
        System.out.println("Hello from a thread!");
    }
    
}
