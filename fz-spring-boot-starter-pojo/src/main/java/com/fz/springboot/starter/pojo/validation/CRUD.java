package com.fz.springboot.starter.pojo.validation;

/**
 * the validation group
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/2 9:13
 */
public interface CRUD {

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

}
