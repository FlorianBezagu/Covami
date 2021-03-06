package models;

import java.util.*;

import javax.persistence.*;

import play.data.validation.Required;
import play.db.jpa.*;

@Entity
public class Country extends Model {	
	@Required
	public String name;
	
	public double latMin;
	public double latMax;
	public double longMin;
	public double longMax;
	
	public int height;
	
	public int width;
}
