package chan.shundat.albert.sqlbuilder;

public interface ConditionBuilding<T> {
	T and();
	T group(Condition condition);
	T predicate(Predicate predicate);
	T or();
}