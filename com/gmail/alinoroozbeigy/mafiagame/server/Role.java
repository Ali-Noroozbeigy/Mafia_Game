package com.gmail.alinoroozbeigy.mafiagame.server;

public enum Role {


    DOCTOR (Category.CITIZENS),
    INSPECTOR(Category.CITIZENS),
    CITIZEN(Category.CITIZENS),
    SNIPER(Category.CITIZENS),
    MAYOR(Category.CITIZENS),
    PSYCHOLOGIST(Category.CITIZENS),
    HARDLIFE(Category.CITIZENS),
    GODFATHER(Category.MAFIAS),
    LECTER(Category.MAFIAS),
    SIMPLEMAFIA(Category.MAFIAS);

    private Category category;

    Role (Category category)
    {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}
