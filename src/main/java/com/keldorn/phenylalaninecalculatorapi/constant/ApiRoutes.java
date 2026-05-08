package com.keldorn.phenylalaninecalculatorapi.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApiRoutes {

    private static final String BY_ID = "/{id}";
    public static final String BASE_PATH = "/api/v1";
    public static final String AUTH_PATH = BASE_PATH + "/auth";
    public static final String USER_PATH = BASE_PATH + "/user/me";
    public static final String FOOD_PATH = BASE_PATH + "/food";
    public static final String FOOD_PATH_BY_ID = FOOD_PATH + BY_ID;
    public static final String FOOD_TYPE_PATH = BASE_PATH + "/food-type";
    public static final String FOOD_TYPE_PATH_BY_ID = FOOD_TYPE_PATH + BY_ID;
    public static final String DAILY_INTAKE_PATH = BASE_PATH + "/daily-intake";
    public static final String FOOD_CONSUMPTION_PATH = BASE_PATH + "/food-consumption";
    public static final String FOOD_CONSUMPTION_PATH_BY_ID = FOOD_CONSUMPTION_PATH + BY_ID;

}
