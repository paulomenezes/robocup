package simple_soccer_lib.utils;


public class Vector2D {
	private double x;
	private double y;
	
	public Vector2D(double x,double y){
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(Vector2D other){
		this.x = other.x;
		this.y = other.y;
	}
	
	public Vector2D(){
		
	}
	
	public Vector2D(double degreeAngle) {
		double radAngle = (degreeAngle * Math.PI) / 180.0d;
		this.x = Math.cos(radAngle);
		this.y = Math.sin(radAngle);
	}

	public void normalizeIn() {
		double length = magnitude();
		if (length != 0) {
			this.x /= length;
			this.y /= length;
		}
	}
	
	public Vector2D normalize(double magnitude) {
		Vector2D v2 = new Vector2D();
		if (magnitude != 0) {
			v2.x = this.x/magnitude;
			v2.y = this.y/magnitude;
		}
		return v2;
	}
	
	public void multiplyIn(double d) {
		this.x *= d;
		this.y *= d;	
	}
	
	public Vector2D multiply(double d) {
		Vector2D v2 = new Vector2D(this.x * d, this.y * d);
	    return v2;	
	}
	
	public void sumIn(Vector2D other) {
		this.x += other.x;
		this.y += other.y;
	}
	
	public Vector2D sum(Vector2D other) {
		Vector2D v2 = new Vector2D(this.x + other.x, this.y + other.y);
	    return v2;
	}
	
	public void subIn(Vector2D other) {
		this.x -= other.x;
		this.y -= other.y;
	}

	public Vector2D sub(Vector2D other) {
		Vector2D v2 = new Vector2D(this.x - other.x, this.y - other.y);
	 	return v2;
	}

	public double magnitude() {
		return Math.sqrt(this.x*this.x + this.y*this.y);
	}
	
	public Vector2D rotate(double angle) {
		double x = (this.x * Math.cos(angle)) - (this.y * Math.sin(angle));
		double y = (this.x * Math.sin(angle)) + (this.y * Math.cos(angle));
		Vector2D v2 = new Vector2D(x,y);
		return v2;
	}
	
	public void rotateIn(double angle) {
		this.x = (this.x * Math.cos(angle)) - (this.y * Math.sin(angle));
		this.y = (this.x * Math.sin(angle)) + (this.y * Math.cos(angle));
	}
	
	public static double distance(Vector2D vector1, Vector2D vector2){
		return Math.sqrt(Math.pow(vector1.x-vector2.x, 2) + Math.pow(vector1.y-vector2.y, 2));
	}
	
	double distanceIn(Vector2D other) {
		return Math.sqrt(Math.pow(this.x-other.x, 2) + Math.pow(this.y-other.y, 2));
	}
	
	double distanceSquared(Vector2D other) { //para comparar (e.g. achar o mais perto), não precisa tirar a raiz quadrada
		return -1;
	}
	
	/**
	 * @deprecated Use {@link #multiplyIn(double)}
	 * */
	@Deprecated
	public void scaleIn(double scale){
		multiplyIn(scale);
	}
	
	/**
	 * @deprecated Use {@link #multiply(double)}
	 * */
	@Deprecated
	public Vector2D scale(double scale){
		return multiply(scale);
	}	
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public static Vector2D[] intersections(Vector2D one, Vector2D two, double done, double dtwo){
		Vector2D[] vectors = null;
		double d = distance(one,two);
		
		if(d>(done+dtwo)) return vectors;
		else if(d<(done-dtwo) ||d<(done-dtwo)*-1) return vectors;
		
		double a = (Math.pow(done,2)-Math.pow(dtwo,2)+Math.pow(d,2))/(2*d);
		double h = Math.sqrt(Math.pow(done,2)-Math.pow(a,2));
		Vector2D three = one.sum(two.sub(one).scale(a/d));
		
		double x1 = three.getX() + h*(two.getY() - one.getY())/d;
		double y1 = three.getY() - h*(two.getX() - one.getX())/d;
		
		double x2 = three.getX() - h*(two.getY() - one.getY())/d;
		double y2 = three.getY() + h*(two.getX() - one.getX())/d;
		
		vectors = new Vector2D[2];
		vectors[0] = new Vector2D(x1,y1); 
		vectors[1] = new Vector2D(x2,y2); 
		
		return vectors;
	}
	
	public static Vector2D[] searchInY(Vector2D one, double distance, double y){
		Vector2D[] vectors = new Vector2D[2];
		double c = Math.pow(one.getY()-y, 2)-Math.pow(distance, 2)-Math.pow(one.getX(), 2);
		double x1 = one.getX()+Math.sqrt(Math.pow(one.getX(),2)-c);
		double x2 = one.getX()-Math.sqrt(Math.pow(one.getX(),2)-c);
		vectors[0] = new Vector2D(x1,y);
		vectors[1] = new Vector2D(x2,y);
		return vectors;
	}
	
	public static Vector2D[] searchInX(Vector2D one,double distance, double x){
		Vector2D[] vectors = new Vector2D[2];
		double c = Math.pow(one.getX()-x, 2)-Math.pow(distance, 2)-Math.pow(one.getY(), 2);
		double y1 = one.getY()+Math.sqrt(Math.pow(one.getY(),2)-c);
		double y2 = one.getY()-Math.sqrt(Math.pow(one.getY(),2)-c);
		vectors[0] = new Vector2D(x,y1);
		vectors[1] = new Vector2D(x,y2);
		return vectors;
	}
	
	public static Vector2D newVector(Vector2D one, double distance, double angle){
		double dx = Math.sin(angle)*distance;
		double dy = Math.cos(angle)*distance;
		Vector2D vector = new Vector2D(dx,dy);
		return vector;
	}
			
	//angle of v2 relative to v1 = atan2(v2.y,v2.x) - atan2(v1.y,v1.x)
	//http://www.euclideanspace.com/maths/algebra/vectors/angleBetween/
	public double angleFrom(Vector2D reference) {
		double radAngle = Math.atan2(this.y, this.x) - Math.atan2(reference.y, reference.x);
		return (radAngle * 180.0d) / Math.PI;
	}

	public String toString() {
		return "Vector2D [x=" + x + ", y=" + y + "]";
	}

	public void overwrite(Vector2D other) {
		this.x = other.x;
		this.y = other.y;		
	}
	
	public void overwrite(double x, double y) {
		this.x = x;
		this.y = y;		
	}

	public double distanceTo(Vector2D reference) {
		return this.sub(reference).magnitude();
	}
	
	public Vector2D copy(){
		return new Vector2D(this);
	}
	
	/**
	 * Calculates the point of interception for one object starting at point
	 * <code>a</code> with speed vector <code>v</code> and another object
	 * starting at point <code>b</code> with a speed of <code>s</code>.
	 * 
	 * @see <a
	 *      href="http://jaran.de/goodbits/2011/07/17/calculating-an-intercept-course-to-a-target-with-constant-direction-and-velocity-in-a-2-dimensional-plane/">Calculating
	 *      an intercept course to a target with constant direction and velocity
	 *      (in a 2-dimensional plane)</a>
	 * 
	 * @param a
	 *            start vector of the object to be intercepted
	 * @param v
	 *            speed vector of the object to be intercepted
	 * @param b
	 *            start vector of the intercepting object
	 * @param s
	 *            speed of the intercepting object
	 * @return vector of interception or <code>null</code> if object cannot be
	 *         intercepted or calculation fails
	 * 
	 * @author Jens Seiler
	 */
	public static Vector2D interception(final Vector2D a, final Vector2D v, final Vector2D b, final double s) {
		final double ox = a.x - b.x;
		final double oy = a.y - b.y;
 
		final double h1 = v.x * v.x + v.y * v.y - s * s;
		final double h2 = ox * v.x + oy * v.y;
		double t;
		if (h1 == 0) { // problem collapses into a simple linear equation 
			t = -(ox * ox + oy * oy) / (2*h2);
		} else { // solve the quadratic equation
			final double minusPHalf = -h2 / h1;
 
			final double discriminant = minusPHalf * minusPHalf - (ox * ox + oy * oy) / h1; // term in brackets is h3
			if (discriminant < 0) { // no (real) solution then...
				return null;
			}
 
			final double root = Math.sqrt(discriminant);
 
			final double t1 = minusPHalf + root;
			final double t2 = minusPHalf - root;
 
			final double tMin = Math.min(t1, t2);
			final double tMax = Math.max(t1, t2);
 
			t = tMin > 0 ? tMin : tMax; // get the smaller of the two times, unless it's negative
			if (t < 0) { // we don't want a solution in the past
				return null;
			}
		}
 
		// calculate the point of interception using the found intercept time and return it
		return new Vector2D(a.getX() + t * v.getX(), a.getY() + t * v.getY());
	}
}
