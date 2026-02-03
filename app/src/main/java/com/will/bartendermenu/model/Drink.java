package com.will.bartendermenu.model;

public class Drink {
    private int id;
    private String name;
    private String description;
    private String imagePath; // Caminho onde a foto está salva
    private boolean isSelected; // Se foi selecionado para a próxima degustação

    // Construtores
    public Drink() {
    }

    public Drink(String name, String description) {
        this.name = name;
        this.description = description;
        this.isSelected = false;
    }

    // Getters e Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}