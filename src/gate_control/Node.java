package gate_control;

import models.Vehicle;
public class Node {
    Vehicle element;
    Node next;

    public Node(Vehicle element){
        this.element=element;
        this.next=null;
    }

    public Vehicle getElement(){
        return this.element;
    }

    public Node getNext(){
        return this.next;
    }

    public void setElement(Vehicle el){
        this.element=el;
    }

    public void setNext (Node ne){
        this.next=ne;
    }
}
