package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import lombok.Data;

import java.util.Collection;

@Data
public class TypeTransactionForm {

    private String name;
    private String code;
    private boolean isImport;
    private String type;
    private TypeDeTransaction typeDeTransaction;
    private Collection<TypeDeFichier> fichiers;

    public TypeTransactionForm() {}

    public TypeTransactionForm(TypeDeTransaction typeDeTransaction) {
        name = typeDeTransaction.getName();
        code = typeDeTransaction.getCode();
        isImport = typeDeTransaction.isImport();
        type = typeDeTransaction.getType();
        this.typeDeTransaction = typeDeTransaction;
        fichiers = typeDeTransaction.getTypeDeFichiers();
    }

    public TypeDeTransaction get() {
        if (typeDeTransaction == null) {
            typeDeTransaction = new TypeDeTransaction();
        }
        if (type != null) {
            typeDeTransaction.setType(type);
        }
        if (name != null) {
            typeDeTransaction.setName(name);
        }
        if (code != null) {
            typeDeTransaction.setCode(code);
        }
        typeDeTransaction.setImport(isImport);
        System.out.println("======================================================================================");
        System.out.println(fichiers);
        System.out.println("======================================================================================");
        if (fichiers != null && !fichiers.isEmpty()) {
            for (TypeDeFichier tf : fichiers) {
                if (!tf.getTypeDeTransactions().contains(typeDeTransaction)) {
                    tf.getTypeDeTransactions().add(typeDeTransaction);
                }
            }
        }

        return this.typeDeTransaction;
    }
}
