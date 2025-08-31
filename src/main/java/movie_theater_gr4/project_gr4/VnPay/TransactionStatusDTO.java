package movie_theater_gr4.project_gr4.VnPay;

import groovy.transform.Sealed;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TransactionStatusDTO implements Serializable {
    private String status;
    private String message;
    private String data;

}
