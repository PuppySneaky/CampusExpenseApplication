package com.example.campusexpensemanagerse06304.model;

public class Products {
    private int _id;
    private String _name;
    private String _image;
    private double _price;
    public Products(int id, String name, String image, double price){
        _id = id;
        _name = name;
        _image = image;
        _price = price;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int get_id() {
        return _id;
    }

    public void set_image(String _image) {
        this._image = _image;
    }

    public String get_image() {
        return _image;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_name() {
        return _name;
    }

    public void set_price(double _price) {
        this._price = _price;
    }

    public double get_price() {
        return _price;
    }
}

