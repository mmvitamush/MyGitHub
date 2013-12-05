package com.example.jsongetapp;

public class RssItem {
	private int line;
	private int lineno;
	private long t_date;
	private double celsius;
	private double humidity;

    
    public int getLine() {
        return line;
    } 
    public void setLine(int line) {
        this.line = line;
    }

    public int getLineno() {
        return lineno;
    }
    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
    
    public long getT_date() {
        return t_date;
    }
    public void setT_date(long t_date) {
        this.t_date = t_date;
    }
    
    public double getCelsius() {
        return celsius;
    }
    public void setCelsius(double celsius) {
        this.celsius = celsius;
    }
    
    public double getHumidity() {
        return humidity;
    }
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
