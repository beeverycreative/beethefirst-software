package replicatorg.app.util;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems This file is part of BEESOFT
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. BEESOFT is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * BEESOFT. If not, see <http://www.gnu.org/licenses/>.
 */
public class AutonomousData<X, Y, Z, Q, P> {

    private final X x;
    private final Y y;
    private final Z z;
    private final Q q;
    private final P p;

    public AutonomousData(X x, Y y, Z z, Q q, P p) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.q = q;
        this.p = p;
    }

    public X getEstimatedTime() {
        return this.x;
    }

    public Y getElapsedTime() {
        return this.y;
    }

    public Z getNLines() {
        return z;
    }

    public Q getCurrentNLines() {
        return q;
    }

    public P getP() {
        return p;
    }
}