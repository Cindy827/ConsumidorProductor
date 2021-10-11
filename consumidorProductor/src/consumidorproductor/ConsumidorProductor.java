package consumidorproductor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Cindy
 * 
 *  //consumidor = si hay platillo le resto 1 a la variable
    //consumidor = si NO hay platillo despierto al proveedor y duermo
    //cocinero = duermo esperando a ser llamado
    //cocinero = si llaman, produzco 9 platillos y voy a dormir
 */

public class ConsumidorProductor extends JFrame implements Runnable{
    //int 
    int AnchoVentana = 1200;
    int AltoVentana = 800;
    
    BufferedImage bi;
    //imagenes
    Image fondo, chef, cliente, plato, plato2, chefS, dog, dogH;
    
    private boolean consumidor;
    private int id;
    private static int ultimoC=-1;
    //quiero 5 consumidores y 10 platillos 
    private static int platillo = 0, x1d=0, y1d=0, x2d=0, y2d=0, nC=5, nP=9;//nC=Numero Canibales nP=Numero Platillos
    private static Object lock = new Object();
    
    public ConsumidorProductor(boolean consumidor, int h){
        this.consumidor = consumidor;
        this.id = h;
    }
    public ConsumidorProductor(){
       setSize(AnchoVentana,AltoVentana);
       setDefaultCloseOperation(EXIT_ON_CLOSE);
       setLocationRelativeTo(null);
       setTitle("Ejercicio Consumidor Productor");
       setResizable(false);

       bi      = new BufferedImage(AnchoVentana, AltoVentana,BufferedImage.TYPE_INT_RGB);

       Toolkit herramienta = Toolkit.getDefaultToolkit();
       fondo = herramienta.getImage(getClass().getResource("/img/fondo.jpg"));
       plato = herramienta.getImage(getClass().getResource("/img/plato.png"));
       plato2 = herramienta.getImage(getClass().getResource("/img/plato2.png"));
       chef  = herramienta.getImage(getClass().getResource("/img/chef.png"));
       chefS = herramienta.getImage(getClass().getResource("/img/chefSleep.png"));
       dog   = herramienta.getImage(getClass().getResource("/img/kevin.png"));
       dogH  = herramienta.getImage(getClass().getResource("/img/kevinHungry.png"));
   }
    public static void main(String[] args) {
        new ConsumidorProductor().setVisible(true);
        
        int numHilos = nC + 1;//= Cantidad de consumidores + 1 productor
        
        Thread[] hilos = new Thread[numHilos];

        for(int h=0; h < hilos.length ; h++){//ARREGLO PARA CREAR HILOS
            Runnable runnable = null;
            
            if(h != 0){//todos los demas hilos entran en esta condicion, siendo 9 los restantes
                runnable = new ConsumidorProductor(true,h);//indicamos aqui que es un consumidor
            }
            else{//Cuando el primer hilo sea procesado/lanzado se le indicara que es consumidor, asi aumentan o se aseguran las posibilidades de que entre primero
                runnable = new ConsumidorProductor(false,h);//indica entonces que es un productor
            }
            
            hilos[h] = new Thread(runnable);
            hilos[h].start(); //se inician todos los hilos  
        }        
        for(int h=0; h<hilos.length; h++){
            try{
                hilos[h].join();
            }catch(Exception ex){}
        }
    }
    
       
    @Override
    public void run() {
        //this.repaint();
        while(true){//se crea un bucle infinito
            
            if(consumidor){//diferenciamos si eres consumidor o productor
                consumiendo();   
            }
            else{//si eres productor
                cocinando();
            }
        }
    }
    
    @Override
    public void paint(Graphics g){
        Graphics2D g2d;
        g.drawImage(bi, 0, 0, this);
        g2d = bi.createGraphics();
     
        //System.out.println(platillo);
        g2d.drawImage(fondo , 0   , 0   , 1200 , 800 , 0 , 0 , 1200 , 800 , this);//IMAGEN FONDO
        
        if(platillo == 0){
            g2d.drawImage(chef  , 800 ,  80 , 950 , 300 , 0 , 0 ,  237 , 341 , this);//chef
            g2d.drawImage(dog   , 1000 ,  220 , 1150 , 370 , 0 , 0 , 1600 , 1600 , this);      
        } 
        if(platillo != 0){
            g2d.drawImage(chefS  , 800 ,  80 , 950 , 300 , 0 , 0 ,  237 , 341 , this);//chef mimido
        }
        if(platillo > 0){
            //System.out.println(platillo);
            
            int x1=100  ,  y1=280, x2=200, y2=380;
            
            for(int a=0; a < platillo; a++){
                g2d.drawImage(plato2 , x1 , y1 ,  x2 , y2 , 0 , 0 ,  680 , 680 , this);//plato 2
                x1=x1+100; x2=x2+100;
            }
      
            g2d.drawImage(dogH , x1d , y1d ,  x2d , y2d , 0 , 0 ,  1600 , 1600 , this);//Periito Kevin comiendo   
        }
        
        repaint();
    }
    
    private void cocinando() {
        
        synchronized(lock){
            //repaint();
            if(platillo == 0){//si la mesa está vacia
                if(ultimoC != -1){//si hay consumidor en espera
                    try {
                    Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConsumidorProductor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    platillo = nP;
                    x1d=1000; y1d=220; x2d=1150; y2d=370;
                    System.out.println("Soy el chef, he puesto " + platillo + " platos en la mesa");

                    lock.notifyAll();//despertamos a todos para que los consumidores pasen
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {

                    }
                }else{
                    try {
                        //System.out.println("Hola "+ ultimoC);
                        lock.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConsumidorProductor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            
        }
        
    }

    private void consumiendo() {
        synchronized(lock){
            try {
            Thread.sleep(100);
            } catch (InterruptedException ex) { }
            
            if(platillo != 0){//si hay platillos en la mesa

                if(ultimoC != -1){//si hay consumidor en espera
 
                    if(ultimoC==this.id){
                        x1d=x1d-95; x2d=x2d-95;//avance en perrito para que vaya al plato
                        System.out.println("Soy"+this.id + " regrese, me he comido el platillo "+ (platillo));
                        platillo--;
                        System.out.println("                   Quedan " + platillo+ " platos mas");
                        ultimoC=-1;
                        lock.notifyAll();
                        try {
                        Thread.sleep(500);
                        } catch (InterruptedException ex) { }
                    }
                    else{
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ConsumidorProductor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }   
                else{
                    x1d=x1d-95; x2d=x2d-95;//avance en perrito para que vaya al plato
                    System.out.println("Soy "+ this.id +" me he comido el platillo "+ (platillo)+" no hay nadie en espera");
                    platillo--;
                    System.out.println("                   Quedan " + platillo+ " platos mas");

                    lock.notifyAll();//Se despiertan todos los hilos por si quedan platos por consumir

                    try {
                        Thread.sleep(500);
                        lock.wait();
                    }catch (InterruptedException ex) {}
                }                  
            }
            else{//si no hay platillos en la mesa     //crear variable para saber ultimo consumidor crear if que no deje entrar a ningun consumidor a menos que sea el ultimo
                
                if(ultimoC != -1){
                    //System.out.println("Soy " + this.id + " mesa vacia quiero uno");
                    lock.notifyAll();
                    try {
                        lock.wait();
                    } catch (InterruptedException ex) {} 
                }
                else if(ultimoC == -1){
                    ultimoC = this.id;
                    
                    System.out.println("Soy " + this.id + " encontre: "+platillo+" platillos, quiero uno pls :c"+ ultimoC);
                    lock.notifyAll();//al no poder despertar un hilo en concreto se despiertan a todos
                    
                }
                
            }       
        }
    }
}
