package io.github.fbbzl.starter.mybatisplus.frame;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import io.github.fbbzl.starter.mybatisplus.BaseMybatisPlusEntity;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Treats {@code deleted_at} as the logical delete marker:
 * {@code null} means not deleted, any non-null {@link LocalDateTime} means deleted.
 * <p>
 * Works together with {@code @TableLogic(value = "null", delval = "now()")} on
 * {@link BaseMybatisPlusEntity#deletedAt}. The interceptor converts the generated SQL so that
 * {@code deleted_at = null} / {@code deleted_at = 'null'} become {@code deleted_at IS NULL},
 * and {@code now()} / {@code 'now()'} in the SET clause become a JDBC parameter filled with the current
 * {@link LocalDateTime}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/7/16
 */
public class LogicDeleteWithDeleteAtInnerInterceptor extends JsqlParserSupport implements InnerInterceptor
{

    private static final String DELETED_AT_COLUMN = StringUtils.camelToUnderline(BaseMybatisPlusEntity.Fields.deletedAt);
    private static final String DELETED_AT_PARAM  = "_deletedAt";

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout)
    {
        PluginUtils.MPStatementHandler mpStatementHandler = PluginUtils.mpStatementHandler(sh);
        MappedStatement                mappedStatement    = mpStatementHandler.mappedStatement();
        SqlCommandType                 sqlCommandType     = mappedStatement.getSqlCommandType();
        if (sqlCommandType != SqlCommandType.UPDATE && sqlCommandType != SqlCommandType.SELECT) {
            return;
        }

        BoundSql               boundSql    = sh.getBoundSql();
        PluginUtils.MPBoundSql mpBoundSql  = PluginUtils.mpBoundSql(boundSql);
        String                 originalSql = mpBoundSql.sql();

        Context context     = new Context();
        String  parsedSql   = parserMulti(originalSql, context);
        boolean sqlChanged  = !originalSql.equals(parsedSql);

        if (context.value != null || sqlChanged) {
            mpBoundSql.sql(parsedSql);
        }
        if (context.value != null) {
            addDeletedAtParameter(mappedStatement.getConfiguration(), mpBoundSql, boundSql, context);
        }
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj)
    {
        if (select instanceof PlainSelect plainSelect && plainSelect.getWhere() != null) {
            plainSelect.setWhere(replaceDeletedAtEqualsNull(plainSelect.getWhere()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processUpdate(Update update, int index, String sql, Object obj)
    {
        Context context = (Context) obj;

        List<UpdateSet> updateSets = update.getUpdateSets();
        for (int i = 0; i < updateSets.size(); i++) {
            UpdateSet    updateSet = updateSets.get(i);
            List<Column> columns    = updateSet.getColumns();
            for (int j = 0; j < columns.size(); j++) {
                Column     column     = columns.get(j);
                Expression expression = updateSet.getValue(j);
                if (!DELETED_AT_COLUMN.equalsIgnoreCase(column.getColumnName())) {
                    continue;
                }
                if (!isNowExpression(expression)) {
                    continue;
                }
                ((ExpressionList<Expression>) updateSet.getValues()).set(j, new JdbcParameter());
                context.value = LocalDateTime.now();
                context.parameterIndex = countJdbcParametersBefore(updateSets, i, j);
            }
        }

        if (update.getWhere() != null) {
            update.setWhere(replaceDeletedAtEqualsNull(update.getWhere()));
        }
    }

    private int countJdbcParametersBefore(List<UpdateSet> updateSets, int setIndex, int exprIndex)
    {
        int count = 0;
        for (int i = 0; i < setIndex; i++) {
            UpdateSet updateSet = updateSets.get(i);
            for (int j = 0; j < updateSet.getColumns().size(); j++) {
                if (updateSet.getValue(j) instanceof JdbcParameter) {
                    count++;
                }
            }
        }
        for (int j = 0; j < exprIndex; j++) {
            if (updateSets.get(setIndex).getValue(j) instanceof JdbcParameter) {
                count++;
            }
        }
        return count;
    }

    private Expression replaceDeletedAtEqualsNull(Expression expression)
    {
        if (expression instanceof EqualsTo equalsTo) {
            Expression left  = equalsTo.getLeftExpression();
            Expression right = equalsTo.getRightExpression();
            if (isDeletedAtColumn(left) && isNullValue(right)) {
                return new IsNullExpression(left);
            }
            if (isDeletedAtColumn(right) && isNullValue(left)) {
                return new IsNullExpression(right);
            }
            Expression newLeft  = replaceDeletedAtEqualsNull(left);
            Expression newRight = replaceDeletedAtEqualsNull(right);
            if (newLeft != left || newRight != right) {
                EqualsTo newEqualsTo = new EqualsTo();
                newEqualsTo.setLeftExpression(newLeft);
                newEqualsTo.setRightExpression(newRight);
                return newEqualsTo;
            }
            return equalsTo;
        }
        if (expression instanceof AndExpression and) {
            Expression left  = replaceDeletedAtEqualsNull(and.getLeftExpression());
            Expression right = replaceDeletedAtEqualsNull(and.getRightExpression());
            if (left != and.getLeftExpression() || right != and.getRightExpression()) {
                return new AndExpression(left, right);
            }
            return and;
        }
        if (expression instanceof OrExpression or) {
            Expression left  = replaceDeletedAtEqualsNull(or.getLeftExpression());
            Expression right = replaceDeletedAtEqualsNull(or.getRightExpression());
            if (left != or.getLeftExpression() || right != or.getRightExpression()) {
                return new OrExpression(left, right);
            }
            return or;
        }
        if (expression instanceof Parenthesis parenthesis) {
            Expression inner = replaceDeletedAtEqualsNull(parenthesis.getExpression());
            if (inner != parenthesis.getExpression()) {
                return new Parenthesis().withExpression(inner);
            }
            return parenthesis;
        }
        return expression;
    }

    private boolean isDeletedAtColumn(Expression expression)
    {
        return expression instanceof Column column
               && DELETED_AT_COLUMN.equalsIgnoreCase(column.getColumnName());
    }

    private boolean isNullValue(Expression expression)
    {
        return expression instanceof NullValue
               || (expression instanceof StringValue string && "null".equalsIgnoreCase(string.getValue()));
    }

    private boolean isNowExpression(Expression expression)
    {
        if (expression instanceof Function function && "now".equalsIgnoreCase(function.getName())) {
            return true;
        }
        return expression instanceof StringValue string && "now()".equalsIgnoreCase(string.getValue());
    }

    private void addDeletedAtParameter(Configuration configuration, PluginUtils.MPBoundSql mpBoundSql, BoundSql boundSql, Context context)
    {
        ParameterMapping mapping = new ParameterMapping.Builder(configuration, DELETED_AT_PARAM, LocalDateTime.class).build();

        List<ParameterMapping> parameterMappings = new ArrayList<>(mpBoundSql.parameterMappings());
        parameterMappings.add(context.parameterIndex, mapping);
        mpBoundSql.parameterMappings(parameterMappings);

        PluginUtils.setAdditionalParameter(boundSql, Map.of(DELETED_AT_PARAM, context.value));
    }

    static class Context
    {
        LocalDateTime value;
        int           parameterIndex;
    }
}
