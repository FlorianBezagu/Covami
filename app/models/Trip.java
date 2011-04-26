package models;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Required;
import play.db.jpa.*;

@Entity

public class Trip extends Model {
	@Required
	@ManyToOne
    public City from;
	
	@Required
	@ManyToOne
    public City to;
	
	@Required
	public float distance;
	
	@OneToMany
	public List<City> cities;
}
