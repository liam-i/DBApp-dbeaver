/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.meta.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSTypedObjectEx;
import org.jkiss.dbeaver.model.struct.DBSTypedObjectExt3;
import org.jkiss.dbeaver.model.struct.DBSTypedObjectExt4;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OracleTableColumn
 */
public class OracleTableColumn extends JDBCTableColumn<OracleTableBase> implements
    DBSTableColumn, DBSTypedObjectEx, DBSTypedObjectExt3, DBPHiddenObject, DBPNamedObject2, DBSTypedObjectExt4<OracleDataType>, DBPObjectWithLazyDescription
{
    private static final Log log = Log.getLog(OracleTableColumn.class);

    private OracleDataType type;
    private OracleDataTypeModifier typeMod;
    private String comment;
    private boolean hidden;

    public OracleTableColumn(OracleTableBase table)
    {
        super(table, false);
    }

    public OracleTableColumn(
        DBRProgressMonitor monitor,
        OracleTableBase table,
        @NotNull ResultSet dbResult)
        throws DBException
    {
        super(table, true);
        // Read default value first because it is of LONG type and has to be read before others
        setDefaultValue(JDBCUtils.safeGetString(dbResult, "DATA_DEFAULT"));

        setName(JDBCUtils.safeGetString(dbResult, "COLUMN_NAME"));
        setOrdinalPosition(JDBCUtils.safeGetInt(dbResult, "COLUMN_ID"));
        this.typeName = JDBCUtils.safeGetString(dbResult, "DATA_TYPE");
        this.type = OracleDataType.resolveDataType(
            monitor,
            getDataSource(),
            JDBCUtils.safeGetString(dbResult, "DATA_TYPE_OWNER"),
            this.typeName);
        this.typeMod = OracleDataTypeModifier.resolveTypeModifier(JDBCUtils.safeGetString(dbResult, "DATA_TYPE_MOD"));
        if (this.type != null) {
            this.typeName = type.getFullyQualifiedName(DBPEvaluationContext.DDL);
            this.valueType = type.getTypeID();
        }
        if (typeMod == OracleDataTypeModifier.REF) {
            this.valueType = Types.REF;
        }
        String charUsed = JDBCUtils.safeGetString(dbResult, "CHAR_USED");
        setMaxLength(JDBCUtils.safeGetLong(dbResult, "C".equals(charUsed) ? "CHAR_LENGTH" : "DATA_LENGTH"));
        setRequired(!"Y".equals(JDBCUtils.safeGetString(dbResult, "NULLABLE")));
        Integer scale = JDBCUtils.safeGetInteger(dbResult, "DATA_SCALE");
        if (scale == null) {
            // Scale can be null in case when type was declared without parameters (examples: NUMBER, NUMBER(*), FLOAT)
            if (this.type != null && this.type.getScale() != null) {
                scale = this.type.getScale();
            }
        }
        setScale(scale);
        setPrecision(JDBCUtils.safeGetInteger(dbResult, "DATA_PRECISION"));
        this.hidden = JDBCUtils.safeGetBoolean(dbResult, "HIDDEN_COLUMN", OracleConstants.YES);
    }

    @NotNull
    @Override
    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Property(viewable = true, editable = true, updatable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(this);
    }

    @Override
    protected void validateTypeName(String typeName) throws DBException {
        if (getDataSource().resolveDataType(new VoidProgressMonitor(), typeName) == null) {
            throw new DBException("Bad data type name " + typeName);
        }
    }

    @Nullable
    @Override
    //@Property(viewable = false, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 21, listProvider = ColumnDataTypeListProvider.class)
    public OracleDataType getDataType() {
        return type;
    }

    @Override
    public void setDataType(OracleDataType type)
    {
        this.type = type;
        this.typeName = type == null ? "" : type.getFullyQualifiedName(DBPEvaluationContext.DDL);
    }

    @Property(viewable = true, order = 30)
    public OracleDataTypeModifier getTypeMod()
    {
        return typeMod;
    }

    //@Property(name = "Data Type", viewable = true, editable = true, updatable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

    @Property(viewable = false, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 40)
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    @Override
    @Property(viewable = false, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 41)
    public Integer getPrecision()
    {
        return super.getPrecision();
    }

    @Override
    @Property(viewable = false, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 42)
    public Integer getScale()
    {
        return super.getScale();
    }

    @Property(viewable = true, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 50)
    @Override
    public boolean isRequired()
    {
        return super.isRequired();
    }

    @Property(viewable = true, editableExpr = "!object.table.view", updatableExpr = "!object.table.view", order = 70)
    @Override
    public String getDefaultValue()
    {
        return super.getDefaultValue();
    }

    @Override
    public boolean isAutoGenerated()
    {
        return false;
    }

    public static class CommentLoadValidator implements IPropertyCacheValidator<OracleTableColumn> {
        @Override
        public boolean isPropertyCached(OracleTableColumn object, Object propertyId)
        {
            return object.comment != null;
        }
    }

    @Nullable
    @Override
    public String getDescription(DBRProgressMonitor monitor) {
        return getComment(monitor);
    }

    @Property(viewable = true, editable = true, updatable = true, length = PropertyLength.MULTILINE, order = 100)
    @LazyProperty(cacheValidator = CommentLoadValidator.class)
    public String getComment(DBRProgressMonitor monitor) {
        if (isPersisted() && comment == null) {
            // Load comments for all table columns
            getTable().loadColumnComments(monitor);
        }
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    void cacheComment() {
        if (this.comment == null) {
            this.comment = "";
        }
    }

    @Nullable
    @Override
    public String getDescription() {
        return comment;
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }

    public static class ColumnDataTypeListProvider implements IPropertyValueListProvider<OracleTableColumn> {

        @Override
        public boolean allowCustomValue()
        {
            return false;
        }

        @Override
        public Object[] getPossibleValues(OracleTableColumn column)
        {
            List<DBSDataType> dataTypes = new ArrayList<>(column.getTable().getDataSource().getLocalDataTypes());
            if (!dataTypes.contains(column.getDataType())) {
                dataTypes.add(column.getDataType());
            }
            Collections.sort(dataTypes, DBUtils.nameComparator());
            return dataTypes.toArray(new DBSDataType[dataTypes.size()]);
        }
    }

}
