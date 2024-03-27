package com.inclunav.iwayplus;

public enum MessagePriority {
    L0(3),
    L1(2),
    L2(1),
    L3(0);
    private int priorityVal;

    MessagePriority(int priority){
        this.priorityVal = priority;
    }

    public int getPriorityValue(){
        return priorityVal;
    }


}
