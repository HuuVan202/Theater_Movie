package movie_theater_gr4.project_gr4.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.dto.CinemaRoomDTO;
import movie_theater_gr4.project_gr4.dto.SeatMapDTO;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import movie_theater_gr4.project_gr4.model.Seat;
import movie_theater_gr4.project_gr4.service.CinemaRoomService;
import movie_theater_gr4.project_gr4.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/cinema-room")
public class CinemaRoomController {
    @Autowired
    private CinemaRoomService cinemaRoomService;
    @Autowired
    private SeatService seatService;

//    @GetMapping("")
//    public String listRooms(Model model) {
//        List<CinemaRoom> cinemaRooms = cinemaRoomService.findAll();
//        Map<Long, Integer> activeSeatCounts = cinemaRoomService.getActiveSeatCountByRoom();
//
//        model.addAttribute("cinemaRooms", cinemaRooms);
//        model.addAttribute("activeSeatCounts", activeSeatCounts);
//
//        return "admin/room/cinema-room";
//    }

    @GetMapping("")
    public String listRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<CinemaRoom> roomPage = cinemaRoomService.findAllPaginated(page, size);
        List<CinemaRoom> cinemaRooms = roomPage.getContent();
        Map<Long, Integer> activeSeatCounts = cinemaRoomService.getActiveSeatCountByRoom();

        model.addAttribute("cinemaRooms", cinemaRooms);
        model.addAttribute("activeSeatCounts", activeSeatCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());

        return "admin/room/cinema-room";
    }


    @GetMapping("/create-seat-map")
    public String showSeatMapForm(Model model) {
        model.addAttribute("cinemaRoomDTO", new CinemaRoomDTO());
        model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
        model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
        model.addAttribute("error", null);
        return "admin/room/create-cinema-room";
    }

    @PostMapping("/save-seat-map")
    @Transactional
    public String saveSeatMap(@Valid @ModelAttribute CinemaRoomDTO cinemaRoomDTO,
                              BindingResult result,
                              Model model) {
        System.out.println("Received CinemaRoomDTO: " + cinemaRoomDTO);
        System.out.println("BindingResult errors: " + result.getAllErrors());

        if (result.hasErrors()) {
            System.out.println("Validation errors found: ");
            result.getAllErrors().forEach(error -> System.out.println("Error: " + error.getObjectName() + " - " + error.getDefaultMessage()));
            model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
            model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
            model.addAttribute("error", "Dữ liệu không hợp lệ: " + result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
            System.out.println("Returning to create-cinema-room with error: " + model.getAttribute("error"));
            return "admin/room/create-cinema-room";
        }

        System.out.println("Seat Prices before saving: " + cinemaRoomDTO.getSeatPrices());

        try {
            System.out.println("Attempting to save CinemaRoom...");
            CinemaRoom savedRoom = cinemaRoomService.saveCinemaRoom(cinemaRoomDTO);
            System.out.println("CinemaRoom saved successfully: " + savedRoom.getRoomId());

            System.out.println("Attempting to save seats...");
            // Bỏ qua validate trong SeatMapDTO, xử lý trong SeatService
            seatService.saveSeats(savedRoom, cinemaRoomDTO.getSeatMap(), cinemaRoomDTO.getSeatPrices());
            System.out.println("Seats saved successfully for room: " + savedRoom.getRoomId());
            return "redirect:/admin/cinema-room";
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("IllegalArgumentException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("DataIntegrityViolationException: " + e.getMessage());
            model.addAttribute("error", "Trùng tên phòng hoặc dữ liệu không hợp lệ!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("General Exception: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
        model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
        System.out.println("Returning to create-cinema-room with error: " + model.getAttribute("error"));
        return "admin/room/create-cinema-room";
    }

    @GetMapping("/view/{id}")
    public String viewCinemaRoom(@PathVariable("id") Long id, Model model) {
        try {
            CinemaRoom cinemaRoom = cinemaRoomService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Phòng chiếu không tồn tại"));
            CinemaRoomDTO cinemaRoomDTO = cinemaRoomService.findDTOById(id);
            if (cinemaRoomDTO == null) {
                model.addAttribute("error", "Phòng chiếu không tồn tại");
                return "redirect:/admin/cinema-room";
            }
            if (cinemaRoomDTO.getSeatPrices() == null) {
                cinemaRoomDTO.setSeatPrices(new HashMap<>());
                cinemaRoomDTO.getSeatPrices().put("normal", 100000.0); // Giá mặc định
                cinemaRoomDTO.getSeatPrices().put("vip", 150000.0);
                cinemaRoomDTO.getSeatPrices().put("couple", 200000.0);
            }
            model.addAttribute("cinemaRoom", cinemaRoom);
            model.addAttribute("cinemaRoomDTO", cinemaRoomDTO);
            return "admin/room/view-cinema-room";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/cinema-room";
        }
    }

    @GetMapping("/check-used/{id}")
    @ResponseBody
    public Map<String, Boolean> checkRoomUsed(@PathVariable("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isUsed = cinemaRoomService.isRoomUsedInShowtime(id);
        response.put("isUsed", isUsed);
        return response;
    }

    @GetMapping("/edit/{id}")
    public String showEditSeatMapForm(@PathVariable("id") Long id, Model model,
                                      RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            CinemaRoomDTO cinemaRoomDTO = cinemaRoomService.findDTOById(id);
            if (cinemaRoomDTO == null) {
                model.addAttribute("error", "Phòng chiếu không tồn tại");
                return "redirect:/admin/cinema-room";
            }

            boolean isRoomUsed = cinemaRoomService.isRoomUsedInShowtime(id);
            if (isRoomUsed) {
                String referer = request.getHeader("Referer");
                String redirectUrl = referer != null && referer.contains("/view/") ? "/admin/cinema-room/view/" + id : "/admin/cinema-room";
                redirectAttributes.addFlashAttribute("warning", "Phòng chiếu đang được sử dụng trong lịch chiếu. Không thể chỉnh sửa.");
                return "redirect:" + redirectUrl;
            }

            // Đảm bảo seatPrices được truyền vào nếu có
            if (cinemaRoomDTO.getSeatPrices() == null) {
                cinemaRoomDTO.setSeatPrices(new HashMap<>());
                cinemaRoomDTO.getSeatPrices().put("normal", 100000.0); // Giá mặc định
                cinemaRoomDTO.getSeatPrices().put("vip", 150000.0);
                cinemaRoomDTO.getSeatPrices().put("couple", 200000.0);
            }
            model.addAttribute("cinemaRoomDTO", cinemaRoomDTO);
            model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
            model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
            return "admin/room/edit-cinema-room";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/cinema-room";
        }
    }

    @PostMapping("/update-seat-map")
    @Transactional
    public String updateSeatMap(@Valid @ModelAttribute CinemaRoomDTO cinemaRoomDTO,
                                BindingResult result,
                                Model model) {
        System.out.println("Received CinemaRoomDTO for update: " + cinemaRoomDTO);
        System.out.println("BindingResult errors: " + result.getAllErrors());

        if (result.hasErrors()) {
            System.out.println("Validation errors found:");
            result.getAllErrors().forEach(error ->
                    System.out.println("Error: " + error.getObjectName() + " - " + error.getDefaultMessage()));
            model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
            model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
            model.addAttribute("error", "Dữ liệu không hợp lệ: " + result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", ")));
            System.out.println("Returning to edit-cinema-room with error: " + model.getAttribute("error"));
            return "admin/room/edit-cinema-room";
        }

        try {
            Long roomId = cinemaRoomDTO.getRoomId();
            if (roomId == null) {
                throw new IllegalArgumentException("ID phòng không được để trống");
            }

            // Tìm phòng hiện tại
            CinemaRoom existingRoom = cinemaRoomService.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Phòng với ID " + roomId + " không tồn tại"));
            System.out.println("Found existing room: " + existingRoom.getRoomId());

            // Cập nhật thông tin phòng (không cần xử lý giữ lại từng thuộc tính nếu form đã bind đầy đủ)
            System.out.println("Attempting to update CinemaRoom...");
            cinemaRoomService.update(cinemaRoomDTO);
            System.out.println("CinemaRoom updated successfully: " + cinemaRoomDTO.getRoomId());

            // Cập nhật ghế
            System.out.println("Attempting to update seats...");
            seatService.updateSeats(existingRoom, cinemaRoomDTO.getSeatMap(), cinemaRoomDTO.getSeatPrices());
            System.out.println("Seats updated successfully for room: " + existingRoom.getRoomId());

            return "redirect:/admin/cinema-room";
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("IllegalArgumentException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("DataIntegrityViolationException: " + e.getMessage());
            model.addAttribute("error", "Trùng tên phòng hoặc dữ liệu không hợp lệ!");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println("General Exception: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        model.addAttribute("roomTypes", Arrays.asList("2D", "3D", "IMAX", "4DX"));
        model.addAttribute("seatTypes", Arrays.asList("Normal", "VIP", "Couple", "Staircase", "Empty"));
        System.out.println("Returning to edit-cinema-room with error: " + model.getAttribute("error"));
        return "admin/room/edit-cinema-room";
    }


    @GetMapping("/activate/{id}")
    public String activateRoom(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            cinemaRoomService.updateStatus(id, 1);
            redirectAttributes.addFlashAttribute("success", "Phòng chiếu đã được kích hoạt thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể kích hoạt phòng chiếu: " + e.getMessage());
        }
        return "redirect:/admin/cinema-room";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivateRoom(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            // Kiểm tra phòng chiếu có tồn tại không
            CinemaRoomDTO cinemaRoomDTO = cinemaRoomService.findDTOById(id);
            if (cinemaRoomDTO == null) {
                redirectAttributes.addFlashAttribute("error", "Phòng chiếu không tồn tại");
                return "redirect:/admin/cinema-room";
            }

            // Kiểm tra phòng chiếu có đang được sử dụng trong lịch chiếu không
            boolean isRoomUsed = cinemaRoomService.isRoomUsedInShowtime(id);
            if (isRoomUsed) {
                String referer = request.getHeader("Referer");
                String redirectUrl = referer != null && referer.contains("/view/") ? "/admin/cinema-room/view/" + id : "/admin/cinema-room";
                redirectAttributes.addFlashAttribute("warning", "Phòng chiếu đang được sử dụng trong lịch chiếu. Không thể vô hiệu hóa.");
                return "redirect:" + redirectUrl;
            }

            // Tiến hành vô hiệu hóa nếu không có vấn đề gì
            cinemaRoomService.updateStatus(id, 0);
            redirectAttributes.addFlashAttribute("success", "Phòng chiếu đã được vô hiệu hóa thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể vô hiệu hóa phòng chiếu: " + e.getMessage());
        }
        return "redirect:/admin/cinema-room";
    }
}
