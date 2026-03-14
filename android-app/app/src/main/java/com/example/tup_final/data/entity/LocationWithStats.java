package com.example.tup_final.data.entity;

/**
 * POJO que agrupa LocationEntity con estadísticas de tests.
 * Usado para la lista de ubicaciones (T4.1.1).
 */
public class LocationWithStats {

    public final LocationEntity location;
    public final int testCount;
    public final int completedTestCount;

    public LocationWithStats(LocationEntity location, int testCount, int completedTestCount) {
        this.location = location;
        this.testCount = testCount;
        this.completedTestCount = completedTestCount;
    }

    /**
     * Completa si no tiene tests o si todos los tests están completados.
     */
    public boolean isComplete() {
        return testCount == 0 || completedTestCount == testCount;
    }
}
