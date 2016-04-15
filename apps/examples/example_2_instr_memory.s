# ------------------------------------------------------------------
# Example program with memory instructions.
# ------------------------------------------------------------------

        .text
        .global main
main:

# Memory acesses

        # direct addressing
        lw      t0, 0x200(zero) # load the value from memory address
                                # 0x200 into register t0

        # indirect addressing
        add     t1, zero, 0x200 # write 0x200 into register t1
        lw      t2, 0(t1)       # loads the value from memory address 0x200
                                # (address obtained from register t1) into 
                                # register t2

        # indirect addressing with displacement
        lw      t3, 0x10(t1)    # load the value from memory address 0x210
                                # (0x10 + content of register t1) into
                                # register t3

        sw      t2, 0(t1)       # write the content of register t2 to memory
                                # address 0x200 (obtained from register t1)


# end of program
        add     a0, zero, 0     # exit code 0
        add     a7, zero, 93    # sycall 93: exit
        scall
