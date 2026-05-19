package com.pe.limon.api.gateway.admin.events.dto.get;

import com.pe.limon.api.gateway.nosecure.events.get.TicketDTO;
import com.pe.limon.api.gateway.client.users.profile.dto.ProfileDTO;


public record EventInfoDTO (
   EventDTO event,
   ProfileDTO profile,
   TicketDTO ticketType
){

}
