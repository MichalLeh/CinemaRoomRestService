package cinema.presentation;

import cinema.model.Stats;
import cinema.model.ScreenRoom;
import cinema.model.Seat;
import cinema.model.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
public class SeatController {
	private final ScreenRoom screenRoom = new ScreenRoom(9, 9);
	private Stats stats = new Stats(screenRoom.getTotalRows(), screenRoom.getTotalColumns());
	public SeatController() {}
	@GetMapping("/seats")
	public ScreenRoom getSeat(){
		return screenRoom;
	}
	@PostMapping("/purchase")
	public synchronized ResponseEntity<?> postSeat(@RequestBody Seat seat) {
		if ((seat.getRow() >= 1 && seat.getRow() <= 9) && (seat.getColumn() >= 1 && seat.getColumn() <= 9)) {
			if (screenRoom.getAvailableSeats().contains(seat)) {
				Ticket ticket = new Ticket(seat);

				screenRoom.removeFromAvailableSeats(seat);
				screenRoom.addIntoPurchasedSeats(ticket.getToken(), seat);

				stats.updateStats(-1, 1, seat.getPrice());

				return new ResponseEntity<>(ticket, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(Map.of("error", "The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(Map.of("error", "The number of a row or a column is out of bounds!"), HttpStatus.BAD_REQUEST);
		}
	}
	@PostMapping("/return")
	public synchronized ResponseEntity<?> postReturn(@RequestBody Ticket ticket){
		if(screenRoom.getPurchasedSeats().containsKey(ticket.getToken())){
			int ticketPrice = screenRoom.getPurchasedSeats().get(ticket.getToken()).getPrice();

			stats.updateStats(1, -1, -ticketPrice);
			screenRoom.addAvailableSeat(screenRoom.getPurchasedSeats().get(ticket.getToken()));

			return ResponseEntity.status(HttpStatus.OK)
					.body(Collections.singletonMap("returned_ticket", screenRoom.getPurchasedSeats().get(ticket.getToken())));
		}
		return new ResponseEntity<>(Map.of("error", "Wrong token!"), HttpStatus.BAD_REQUEST);
	}
	@PostMapping("/stats")
	public ResponseEntity<?> postStats(@RequestParam(required = false) String password){
		if (password == null || !password.equals("super_secret")){
			return new ResponseEntity<>(Map.of("error", "The password is wrong!"), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(stats, HttpStatus.OK);
	}
}
