package com.pe.limon.api.gateway.nosecure.events.get;

import com.pe.limon.api.gateway.client.users.profile.dto.ProfileDTO;


public record EventInfoDTO (
   EventDTO event,
   ProfileDTO organizerProfile
){

}
