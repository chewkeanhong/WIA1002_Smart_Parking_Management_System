package gate_control;

import models.Vehicle;
public class EntryQueue {
    private Node front;
    private Node rear;
    private int size;

    public EntryQueue(){
        this.front=null;
        this.rear=null;
        size=0;
    }


    public void enqueue(Vehicle vehicle){
        Node newVec=new Node(vehicle);
        size++;
        if(isEmpty()){
            front=newVec;
            rear=newVec;
            return;
        }
        rear.setNext(newVec);
        rear=newVec;
        return;
    }

    public Vehicle dequeue(){
        if(isEmpty()){
            return null;
        }
        Node temp=front;
        if(front==rear){
            front=null;
            rear=null;
            size--; //also can set to 0
            return temp.getElement();   
        }
        front=front.getNext();
        size--;
        return temp.getElement();

    }

    public Vehicle peek(){
        if(front==null){
            return null;
        }
        return this.front.getElement();
    }

    public boolean isEmpty(){
        return front==null;
    }

    public int size(){
        return this.size;
    }

    public void displayQueue(){
        if(isEmpty()){
            System.out.println("Queue is empty");
            return;
        }
        Node current=front;
        while(current!=null){
            if(current.getNext()!=null){
                System.out.print(current.getElement()+",");
            }
            else{
                System.out.print(current.getElement());
            }
            current=current.getNext();
        }
        return;
    }

    public Vehicle getFrontVehicle(){
        if(isEmpty()){
            return null;
        }
        return this.front.getElement();
    }

}
