package com.akouma.veyuzwebapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "veyuz_domiciliation_transaction")
public class DomiciliationTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Transaction transaction;

    @JoinColumn(name = "domiciliation_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Domiciliation domiciliation;

    private float montant;

}
