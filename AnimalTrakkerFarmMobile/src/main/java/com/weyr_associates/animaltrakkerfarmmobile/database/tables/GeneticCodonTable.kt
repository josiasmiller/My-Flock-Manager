package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCodonTable.CodonTableColumns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.Codon

class GeneticCodonTable private constructor(val codon: Codon) : TableSpec<CodonTableColumns> {

    companion object {
        fun from(codon: Codon): GeneticCodonTable = GeneticCodonTable(codon)
    }

    @Suppress("PropertyName")
    val NAME = "genetic_codon${codon.code}_table"

    // We want this to appear like a standard Table def with a Columns object
    // But it is a property and must have it's JVM name overridden to avoid
    // clashing at the JVM level with the "columns" property that is
    // overridden for TableSpec.
    @Suppress("PropertyName")
    @get:JvmName("CodonTableColumns")
    val Columns = CodonTableColumns(codon)

    class CodonTableColumns(codon: Codon) {
        @Suppress("PropertyName")
        val ID = Column.NotNull("id_geneticcodon${codon.code}id")

        @Suppress("PropertyName")
        val ALLELES = Column.NotNull("codon${codon.code}_alleles")

        val ORDER = Column.NotNull("codon${codon.code}_display_order")

        @Suppress("PropertyName")
        val CREATED get() = Column.NotNull("created")

        @Suppress("PropertyName")
        val MODIFIED get() = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: CodonTableColumns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
