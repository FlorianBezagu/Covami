package controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Announcement;
import models.City;
import models.Member;
import models.PendingAnnouncement;
import models.Trip;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

// TODO: Empêcher de poster une annonce si l'utilisateur n'a pas enregistré de véhicule
@With(Secure.class)
public class Announcements extends Controller {

	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			Member user = Member.find("byEmail", Security.connected()).first();
			renderArgs.put("user", user);
			renderArgs.put("security", Security.connected());
		}
	}

	public static void index() {
		Announcements.list();
	}

	public static void add(Announcement announcement) {
		Member member = Member.find("byEmail", Security.connected()).first();

		// if (announcement == null) { announcement = new Announcement(); }
		// TODO: Prendre en compte announcement dans le form, s'il existe

		renderArgs.put("vehicles", member.vehicles);
		renderArgs.put("cities", City.findAll());
		// renderArgs.put("annoucement", announcement);

		render();
	}

	public static void addPost(Announcement announcement, String startDate)
			throws ParseException {

		if (announcement == null || announcement.trip == null) {
			Announcements.add(null);
		}

		Trip trip = announcement.trip;

		// Préciser les City dans Trip
		// (elles ne sont pas automatiquement remplies)
		trip.from = City.findById(trip.getFrom().id);
		trip.to = City.findById(trip.to.id);

		if (trip.from.equals(trip.to)) {
			flash.error("announcements.sameCity");

			// BUG: Si on met Announcements.add(announcement); play bug !
			// TODO: Reporter le bug
			Announcements.add(new Announcement());
		}

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT, Locale.FRENCH);
		announcement.startDate = df.parse(startDate);

		announcement.kilometers = trip.from.distanceBetween(trip.to);
		announcement.publicationDate = new Date();
		announcement.member = Member.find("byEmail", Security.connected())
				.first();

		// FIXME: Changer la valeur de calcul du prix
		announcement.totalCost = announcement.kilometers * 1.5;

		// Recherche du chemin via aStar
		trip.generatePath();

		if (announcement.trip.validateAndSave()
				&& announcement.validateAndSave()) {
			flash.success("announcements.successWhileSaving");
			Announcements.list();

		} else {
			for (play.data.validation.Error e : Validation.errors()) {
				System.out.println(e);
			}
			flash.error("announcements.errorWhileSaving");
			Announcements.add(new Announcement());
		}

	}

	public static void list() {
		Member member = Member.find("byEmail", Security.connected()).first();

		List<Announcement> annoucements = Announcement.find("byMember_id",
				member.id).fetch();

		renderArgs.put("annoucements", annoucements);
		render();
	}

	public static void see(long id) {

	}

	public static void search() {

	}

	public static void api() {
		List<Announcement> announcements = Announcement.find("startDate >= ?",
				new Date()).fetch();

		renderXml(announcements);
	}

	public static void apply(long id) {
		Member member = Member.find("byEmail", Security.connected()).first();
		Announcement announcement = Announcement.findById(id);

		PendingAnnouncement pending = new PendingAnnouncement(announcement,
				member);
		pending.save();

		announcement.member.pendingAnnouncements.add(pending);
		announcement.member.save();
	}
}