package com.unithub.model;

public enum Category {
    TECNOLOGIA(1L, "Tecnologia"),
    SAUDE(2L, "Saúde"),
    ENGENHARIA(3L, "Engenharia"),
    HUMANAS(4L, "Humanas"),
    EXATAS(5L, "Exatas");

    private final long id;
    private final String descricao;

    Category(long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Category fromId(long id) {
        for (Category categoria : values()) {
            if (categoria.getId() == id) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Categoria inválida para o ID: " + id);
    }
}