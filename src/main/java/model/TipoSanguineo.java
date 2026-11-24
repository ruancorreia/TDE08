package model;

// Enum demonstrativo: os nomes persistidos cabem nos 3 caracteres do modelo original.
public enum TipoSanguineo {
    AP("A+"), AM("A-"), BP("B+"), BM("B-"), ABP("AB+"), ABM("AB-"), OP("O+"), OM("O-");

    private final String rotulo;
    TipoSanguineo(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }

    public static TipoSanguineo deRotulo(String rotulo) throws ModelException {
        for (TipoSanguineo tipo : values()) {
            if (tipo.rotulo.equals(rotulo)) return tipo;
        }
        throw new ModelException("Tipo sanguíneo inválido.");
    }
}
