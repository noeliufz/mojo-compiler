	.data
	.balign 8
L.3:
	.byte 39
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 44
	.byte 32
	.byte 39
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 44
	.byte 32
	.byte 39
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 33
	.byte 0
	.data
	.balign 8
L.4:
	.quad L.3
	.quad 21
	.text
Infinite:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.0:
L.1:
	leaq L.4(%rip),%rax
	movq %rax,t.0
	xorq %rax,%rax
	cmpq %rax,t.0
	je Infinite.badPtr
L.5:
	movq 0(t.0),t.4
	movq t.4,%rdi
	call puts
	movq %rax,t.1
	jmp L.0
L.2:
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
Infinite.badPtr:
	call badPtr
