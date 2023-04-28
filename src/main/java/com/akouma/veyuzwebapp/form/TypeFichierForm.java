package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import lombok.Data;

import java.util.Collection;

@Data
public class TypeFichierForm {

    private String name;
    private boolean isObligatoireForApurement;
    private TypeDeFichier typeDeFichier;
    private Collection<TypeDeTransaction> typeDeTransactions;

    public TypeFichierForm(){}

    public TypeFichierForm(TypeDeFichier typeDeFichier) {
        this.typeDeFichier = typeDeFichier;
        name = typeDeFichier.getName();
        isObligatoireForApurement = typeDeFichier.isObligatoireForApurement();
        typeDeTransactions = typeDeFichier.getTypeDeTransactions();
    }

    public TypeDeFichier get() {
        if (typeDeFichier == null) {
            typeDeFichier = new TypeDeFichier();
        }
        if (name != null) {
            typeDeFichier.setName(name);
        }
        typeDeFichier.setObligatoireForApurement(isObligatoireForApurement);
        if (typeDeTransactions != null && !typeDeTransactions.isEmpty()) {
            typeDeFichier.setTypeDeTransactions(typeDeTransactions);
        }

        return typeDeFichier;
    }

}
