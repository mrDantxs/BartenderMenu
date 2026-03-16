package com.will.bartendermenu.model;

public class Menu {

    private int id;
    private String name;

    public Menu() {
    }

    public Menu(String name) {
        this.name = name;
    }

    public Menu(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters e setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}