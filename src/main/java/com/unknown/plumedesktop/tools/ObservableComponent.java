package com.unknown.plumedesktop.tools;

import java.util.ArrayList;

public class ObservableComponent implements IObservable {
    private final ArrayList<IObserver> observers;
    private IObserver current = null;
    private Boolean current_to_del = false;

    public ObservableComponent() {
        this.observers = new ArrayList<>();
    }

    public void notify(String message) {
        for(int i = 0; i < observers.size(); ++i) {
            current = observers.get(i);
            current.notify(message);
            if(current_to_del) {
                observers.remove(current);
                current_to_del = false;
            }
        }
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        if(observer == current) {
            current_to_del = true;
        } else {
            observers.remove(observer);
        }
    }
}
