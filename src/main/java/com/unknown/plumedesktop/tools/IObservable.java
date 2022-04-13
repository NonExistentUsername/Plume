package com.unknown.plumedesktop.tools;

public interface IObservable {
    public void addObserver(IObserver observer);
    public void removeObserver(IObserver observer);
}
