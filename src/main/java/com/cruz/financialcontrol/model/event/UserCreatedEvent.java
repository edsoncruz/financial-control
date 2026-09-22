package com.cruz.financialcontrol.model.event;

/**
 * Class Name: UserCreatedEvent
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */

public record UserCreatedEvent(Long eventId, Long userId, String name, String email) {}
