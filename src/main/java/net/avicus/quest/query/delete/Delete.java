package net.avicus.quest.query.delete;

import net.avicus.quest.Parameter;
import net.avicus.quest.ParameterizedString;
import net.avicus.quest.Query;
import net.avicus.quest.database.Database;
import net.avicus.quest.database.DatabaseException;
import net.avicus.quest.filter.Filter;
import net.avicus.quest.filter.Filterable;
import net.avicus.quest.parameter.ObjectParameter;
import net.avicus.quest.parameter.OrderParameter;
import net.avicus.quest.parameter.TableParameter;

import java.sql.PreparedStatement;
import java.util.*;

public class Delete implements Query<DeleteResult, DeleteConfig>, Filterable<Delete> {
    private final Database database;
    private final TableParameter table;
    private Filter filter;
    private Parameter limit;
    private List<OrderParameter> order;

    public Delete(Database database, TableParameter table) {
        this.database = database;
        this.table = table;
    }

    public Delete duplicate() {
        Delete copy = new Delete(this.database, this.table);
        copy.filter = this.filter;
        copy.limit = this.limit;
        copy.order = this.order;
        return copy;
    }

    public Delete where(Filter filter) {
        if (this.filter != null) {
            return where(this.filter, filter);
        }
        else {
            Delete select = duplicate();
            select.filter = filter;
            return select;
        }
    }

    public Delete where(Filter filter, Filter... ands) {
        // And the ands
        Filter result = filter;
        for (Filter and : ands) {
            result = result.and(and);
        }

        Delete query = duplicate();
        query.filter = result;
        return query;
    }

    public Delete limit(int limit) {
        return limit(new ObjectParameter(limit));
    }

    public Delete limit(Parameter limit) {
        Delete update = duplicate();
        update.limit = limit;
        return update;
    }

    public Delete order(OrderParameter... order) {
        return order(Arrays.asList(order));
    }

    public Delete order(List<OrderParameter> order) {
        Delete update = duplicate();
        update.order = order;
        return update;
    }

    public ParameterizedString build() {
        StringBuilder sb = new StringBuilder();
        List<Parameter> parameters = new ArrayList<>();

        sb.append("DELETE FROM ");

        sb.append(this.table.getKey());
        parameters.add(this.table);

        if (this.filter != null) {
            sb.append(" WHERE ");
            ParameterizedString filterString = this.filter.build();
            sb.append(filterString.getSql());
            parameters.addAll(filterString.getParameters());
        }

        if (this.order != null) {
            sb.append(" ORDER BY ");
            for (OrderParameter order : this.order) {
                sb.append(order.getKey());
                parameters.add(order);
            }
        }

        if (this.limit != null) {
            sb.append(" LIMIT ");
            sb.append(this.limit.getKey());
            parameters.add(this.limit);
        }

        return new ParameterizedString(sb.toString(), parameters);
    }

    @Override
    public DeleteResult execute(Optional<DeleteConfig> config) throws DatabaseException {
        // The query
        ParameterizedString query = build();

        // Create statement
        PreparedStatement statement = config.orElse(DeleteConfig.DEFAULT).createStatement(this.database, query.getSql());


        System.out.println(build());

        // Add variables (?, ?)
        query.apply(statement, 1);

        return DeleteResult.execute(statement);
    }

    @Override
    public String toString() {
        return "Delete(" + build() + ")";
    }
}
