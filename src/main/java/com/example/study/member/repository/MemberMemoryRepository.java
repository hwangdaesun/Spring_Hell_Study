package com.example.study.member.repository;

import com.example.study.member.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;

@Repository
public class MemberMemoryRepository implements MemberRepository {

    private final HashMap<String, Member> memberHashMap = new HashMap<>();

    @Override
    public void save(Member member) {
        memberHashMap.put(member.getUuid(),member);
    }

    @Override
    public Optional<Member> findByUUDID(String uuid) {
        return Optional.of(memberHashMap.get(uuid));
    }
}
