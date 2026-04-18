package com.fz.starter.pojo.validation.group;

/**
 * the validation group
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 9:13
 */
public interface CRUD
{

    String C   = C.class.getSimpleName();
    String R   = R.class.getSimpleName();
    String U   = U.class.getSimpleName();
    String D   = D.class.getSimpleName();
    String ALL = ALL.class.getSimpleName();

    /**
     * create
     */
    interface C {}

    /**
     * read
     */
    interface R {}

    /**
     * update
     */
    interface U extends CRUD.C {}

    /**
     * delete
     */
    interface D {}

    /**
     * all
     */
    interface ALL extends CRUD.C, CRUD.R, CRUD.U, CRUD.D {}
}
