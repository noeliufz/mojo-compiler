	.text
LargeImmediate.CString:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.8:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je LargeImmediate.CString.badPtr
L.3:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl LargeImmediate.CString.badSub
L.4:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge LargeImmediate.CString.badSub
L.5:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
LargeImmediate.CString.badPtr:
	call badPtr
LargeImmediate.CString.badSub:
	call badSub
	.data
	.balign 8
L.6:
	.byte 78
	.byte 117
	.byte 109
	.byte 98
	.byte 101
	.byte 114
	.byte 32
	.byte 105
	.byte 115
	.byte 58
	.byte 32
	.byte 37
	.byte 120
	.byte 108
	.byte 10
	.byte 0
	.data
	.balign 8
L.7:
	.quad L.6
	.quad 16
	.text
LargeImmediate:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %rbx, -8(%rbp)
L.9:
	movabsq $5124095576030430,%rax
	movq %rax,%rbx
	leaq L.7(%rip),%rax
	movq %rax,%rdi
	call LargeImmediate.CString
#	movq %rax,%rax
	movq %rax,%rdi
	movq %rbx,%rsi
	call printf
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $32,%rsp
	popq %rbp
	ret
LargeImmediate.badSub:
	call badSub
