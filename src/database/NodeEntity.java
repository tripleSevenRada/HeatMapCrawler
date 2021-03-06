package database;

import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = beans.RunnerBean.TABLE_NODES)
public class NodeEntity {

	// finaly I will want this graph format
	// https://www.dropbox.com/s/8et183ufeskkibi/IMG_20171019_194557.jpg?dl=0

	// https://stackoverflow.com/questions/21069687/hibernate-auto-create-database
	// https://stackoverflow.com/questions/43716068/invalid-syntax-error-type-myisam-in-ddl-generated-by-hibernate/43720565

	@Id
	// Indicates that the persistence provider
	// must assign primary keys for the entity using a database identity column.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "shotId")
	private long shotId;
	@Column(name = "lon")
	private double lon;
	@Column(name = "lat")
	private double lat;
	@Column(name = "weight")
	private short weight;

	@ManyToMany
	@JoinTable(name = beans.RunnerBean.TABLE_ADJACENTS)
	private Set<NodeEntity> adjacents;

	private static final transient double EPSILON = 0.00000001d;

	public NodeEntity() {
	}

	public NodeEntity(long shotId, double lon, double lat, short weight, Set<NodeEntity> adjacents) {

		this.weight = weight;
		this.shotId = shotId;
		this.lon = lon;
		this.lat = lat;
		this.adjacents = adjacents;

	}

	public void addToAdj(NodeEntity adj) {
		adjacents.add(adj);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getShotId() {
		return shotId;
	}

	public void setShotId(long shotId) {
		this.shotId = shotId;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double l) {
		this.lon = l;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double l) {
		this.lat = l;
	}

	/**
	 * @return the weight
	 */
	public short getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(short weight) {
		this.weight = weight;
	}

	public Set<NodeEntity> getAdjacents() {
		return adjacents;
	}

	public void setAdjacents(Set<NodeEntity> adj) {
		this.adjacents = adj;
	}

	@Override
	public int hashCode() {
		double lonFloored = Math.floor(lon * 100000.0);
		double latFloored = Math.floor(lat * 100000.0);
		return Objects.hash(lonFloored, latFloored);
	}

	// OBJECT!!!
	@Override
	public boolean equals(Object theOther) {
		// self check
		if (this == theOther)
			return true;
		// null check
		if (theOther == null)
			throw new RuntimeException("equalsMess 1");
		// return false;
		// type check
		if (getClass() != theOther.getClass())
			throw new RuntimeException("equalsMess 2");
		// return false;

		NodeEntity theOtherNe = (NodeEntity) theOther;
		return equalsLonLat(theOtherNe);
	}

	/**
	 * 
	 * @param theOther
	 * @return
	 */
	public boolean equalsLonLat(NodeEntity theOther) {
		boolean lonB = (Math.abs(this.lon - theOther.getLon()) < EPSILON);
		boolean latB = (Math.abs(this.lat - theOther.getLat()) < EPSILON);
		return lonB && latB;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nN O D E    E N T I T Y\n");
		sb.append("|id ").append(id).append(" |shotId ").append(shotId).append(" |lon ").append(lon).append(" |lat ")
				.append(lat).append("\n");
		sb.append("|weight ");
		sb.append("\n|hashCode(): ").append(hashCode());
		sb.append("\n\tadjacents:").append(adjacents.size()).append("\n");

		for (NodeEntity n : adjacents) {
			if (n == this) {
				System.err.println("reference to this in adjacents in NodeEntity.toString()");
				continue;
			}
			sb.append("\n\t" + n.getId() + "---------------------------------");
			sb.append("\n\thash: ").append(n.hashCode());
			sb.append("\n\tshot: ").append(n.getShotId());
			sb.append("\n\tlat: ").append(n.getLat());
			sb.append("\n\tlon:").append(n.getLon());
			sb.append("\n\tweight: ").append(n.getWeight());
		}
		return sb.toString();
	}
}
