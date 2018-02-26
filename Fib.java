// Ryan Morrissey
// CSCI.251.02
// An implementation of generating the Fibonacci sequence through multi threading

/*
	Author: Ryan Morrissey
	Course: CSCI.251.02 - Concepts of Parallel and Distributed Systems
	Date: 9/13/2016
	
	This program will generate the Fibonacci sequence while taking advantage of
	multi threading.
	
	Includes main
*/

import java.math.*;

public class Fib
{
    private static class Monitor
    {
        private BigInteger[] fibArray;
        
        public Monitor(int size)
        {
            this.fibArray = new BigInteger[size];
        }
        
        // To be run once by every thread to place a value into spot i.
        // The fib value is calculated inside this function rather than outside of it.
        // The value passed only matters for the first 2 array values
        public synchronized void putValue(int i, BigInteger value) throws InterruptedException
        {
            // This way to immediately add the values for spots 0 and 1
            if(i == 0)
            {
                fibArray[i] = value;
            }
            else if(i == 1)
            {
                fibArray[i] = value;
            }
            else
            {
                while(fibArray[i - 1] == null || fibArray[0] == null)
                    wait();
                fibArray[i] = fibArray[i - 1].add(fibArray[i - 2]);
            }   
            notifyAll();
        }
        
        // Returns the value of index[i].  Only to be used really for the last index in the array
        public synchronized BigInteger getValue(int i) throws InterruptedException 
        {
            // If the value in the array is null, then the put threads have not finished yet
            while(fibArray[i] == null)
                wait();
            notifyAll();
            return fibArray[i];
        }
    }
    
    // Runnable object that puts numbers into the F array found in Monitor
    private static class Putter implements Runnable
    {
        private Monitor monitor;
        private int i;
        private BigInteger val;
        
        public Putter(Monitor monitor, int i, BigInteger val)
        {
            this.monitor = monitor;
            this.i = i;
            this.val = val;
        }
        
        public void run()
        {
            try
            {
                monitor.putValue(i, val);
            }
            catch (InterruptedException exc){};
        }
    }
    
    // Runnable object that prints the value of Fib Array index n
    private static class Getter implements Runnable
    {
        private Monitor monitor;
        int n;
        
        public Getter(Monitor monitor, int n)
        {
            this.monitor = monitor;
            this.n = n;
        }
        
        public void run()
        {
            try
            {
                BigInteger v = monitor.getValue(n);
                while((v = monitor.getValue(n)) != null)
                {
                    System.out.println(v);
                    System.exit(1);
                }
            }
            catch (InterruptedException exc){};
        }
    }
    
    // Quick way to check if an arg is a valid int
    public static boolean isNumeric(String str)  
    {  
        try  
        {  
            double d = Integer.parseInt(str);  
        }  
        catch(NumberFormatException nfe)  
        {  
            return false;  
        }  
        return true;  
    }
    
    public static void main(String[] args) throws Throwable
    {
        // First to runs some checks to make sure the input is correct
        if(args.length != 3)
            usage();
            
        // Values are just to initialize them
        BigInteger a = new BigInteger("0");
        BigInteger b = new BigInteger("0");
        int n = 0;
        
        // Now to convert args to values we need
        if(isNumeric(args[0]) == true)
            a = new BigInteger(args[0]);
        else
            usage();
        if(isNumeric(args[1]) == true)
            b = new BigInteger(args[1]);
        else
            usage();
        if(isNumeric(args[2]) == true)
        {
            n = Integer.parseInt(args[2]);
            if(n < 0)
                usage();
        }
        else
            usage();
        
        Monitor monitor = new Monitor(n + 1);
        
        // Start the printing thread
        new Thread(new Getter(monitor, n)).start();
        // Now for the N number of putter threads
        for(int i = (n); i >= 0; i--)
        {
            if(i == 0)
            {
                new Thread(new Putter(monitor, i, a)).start();
            }
            else if(i == 1)
                new Thread(new Putter(monitor, i, b)).start();
            else // Passing a as the value since it doesn't matter, it was put in a calculated value
                new Thread(new Putter(monitor, i, a)).start();
        }
  
    }
    
    // Prints usage statement and exits the program
    private static void usage()
    {
        System.err.println("Usage:  java Fib <a> <b> <n>");
        System.exit(1);
    }
}
